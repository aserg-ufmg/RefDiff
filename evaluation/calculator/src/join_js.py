from typing import Optional, Tuple

from pandas import DataFrame, merge
from numpy import select

from src.schema import result_c_schema

REPO_OWNER = "refdiff-study"

def join_table_js_precision(oracle_df: DataFrame, detected_df: DataFrame, does_ignore_line: Optional[bool]=True) -> DataFrame:
    oracle_df = oracle_df.reset_index()
    detected_df = detected_df.reset_index()

    oracle_df = oracle_df.rename(
        columns={
            "Commit": "commit",
            "Rel. Type": "type",
            "index": "oracle index",
        }
    )
    detected_df = detected_df.rename(
        columns={"index": "detected index"}
    )

    if does_ignore_line:
        # Normalize 'before' and 'after' columns by stripping whitespace and removing line numbers (e.g., ":100-123" at the end).
        # Because line numbers may differ between oracle and detected results depending on inclusions of comments and blank lines.
        oracle_df["Location before"] = oracle_df["Location before"].str.replace(
            r":\d+-\d+", "", regex=True
        )
        oracle_df["Location after"] = oracle_df["Location after"].str.replace(
            r":\d+-\d+", "", regex=True
        )
        detected_df["before"] = detected_df["before"].str.replace(
            r":\d+", "", regex=True
        )
        detected_df["after"] = detected_df["after"].str.replace(r":\d+", "", regex=True)

    detected_df["Node type"] = detected_df.apply(
        lambda row: extract_node_type(row["before"]), axis=1
    )
    detected_df["Location before"] = detected_df.apply(
        lambda row: extract_file_path(row["before"]), axis=1
    )
    detected_df["Local name before"] = detected_df.apply(
        lambda row: extract_identifier(row["before"]), axis=1
    )
    detected_df["Location after"] = detected_df.apply(
        lambda row: extract_file_path(row["after"]), axis=1
    )
    detected_df["Local name after"] = detected_df.apply(
        lambda row: extract_identifier(row["after"]), axis=1
    )

    merge_keys = ["commit", "type", "Node type", "Location before", "Local name before", "Location after", "Local name after"]
    merged_df = merge(
        oracle_df, # left
        detected_df, # right
        on=merge_keys,
        how="left",
        indicator=True,  # add '_merge' column (both, left_only, right_only)
    )

    conditions = [
        (merged_df["_merge"] == "both") & (merged_df["Result"] == "TP"),
        (merged_df["_merge"] == "both") & (merged_df["Result"] == "FP"),
        (merged_df["_merge"] == "left_only") & (merged_df["Result"] == "FP"),
        (merged_df["_merge"] == "left_only") & (merged_df["Result"] == "TP"),
    ]
    choices = ["TP", "FP", "TN", "FN"]
    merged_df["detected result"] = select(
        conditions, choices, default="Unknown"
    )

    result_df = DataFrame(columns=result_c_schema.keys())
    result_df["commit url"] = merged_df["Commit URL"]
    result_df['type'] = merged_df['type']
    result_df['before'] = merged_df['before']
    result_df['after'] = merged_df['after']
    result_df["baseline result"] = merged_df["Result"]
    result_df["detected result"] = merged_df[
        "detected result"
    ]
    result_df["equal to baseline"] = result_df["baseline result"] == result_df[
        "detected result"
    ]

    result_df["oracle index"] = merged_df["oracle index"]
    result_df["detected index"] = merged_df["detected index"]
    result_df["note"] = "" # TODO for notes

    return result_df


def join_table_js_recall(
    oracle_df: DataFrame,
    detected_df: DataFrame,
    does_ignore_line: Optional[bool] = True,
) -> DataFrame:
    oracle_df = oracle_df.reset_index()
    detected_df = detected_df.reset_index()

    oracle_df = oracle_df.rename(
        columns={
            "Rel. Type": "type",
            "index": "oracle index",
        }
    )
    detected_df = detected_df.rename(columns={"index": "detected index"})

    if does_ignore_line:
        # Normalize 'before' and 'after' columns by stripping whitespace and removing line numbers (e.g., ":100-123" at the end).
        # Because line numbers may differ between oracle and detected results depending on inclusions of comments and blank lines.
        detected_df["before"] = detected_df["before"].str.replace(
            r":\d+", "", regex=True
        )
        detected_df["after"] = detected_df["after"].str.replace(r":\d+", "", regex=True)

    oracle_df["commit"] = oracle_df.apply(
        lambda row: extract_commit(row["Commit URL"]), axis=1
    )

    detected_df["Node before"] = detected_df.apply(
        lambda row: format_before_after(row["before"]), axis=1
    )
    detected_df["Node After"] = detected_df.apply(
        lambda row: format_before_after(row["after"]), axis=1
    )

    merge_keys = ["commit", "type", "Node before", "Node After"]
    merged_df = merge(
        oracle_df,  # left
        detected_df,  # right
        on=merge_keys,
        # how="left",
        how="outer",
        indicator=True,  # add '_merge' column (both, left_only, right_only)
    )

    conditions = [
        (merged_df["_merge"] == "both"),
        (merged_df["_merge"] == "left_only"),
        (merged_df["_merge"] == "right_only"),
    ]
    choices = ["TP", "FN", "FP"]
    merged_df["detected result"] = select(conditions, choices, default="Unknown")

    result_df = DataFrame(columns=result_c_schema.keys())
    result_df["commit url"] = merged_df["Commit URL"]
    result_df["type"] = merged_df["type"]
    result_df["before"] = merged_df["before"]
    result_df["after"] = merged_df["after"]
    result_df["detected result"] = merged_df["detected result"]
    result_df["equal to baseline"] = (
        result_df["baseline result"] == result_df["detected result"]
    )

    result_df["oracle index"] = merged_df["oracle index"]
    result_df["detected index"] = merged_df["detected index"]
    result_df["note"] = ""  # TODO for notes

    return result_df


def create_commit_url(owner_name: str, repo_name: str, commit: str) -> str:
    return f"https://github.com/{owner_name}/{repo_name}/commit/{commit}"

def extract_node_type(type_str: str) -> str:
    """
    Extract the node type from a string.
    Example: "{Function functionName at src/foo.js})" -> "Function"
    """
    cleaned_string = type_str.strip("{}()")
    node_type, identifier = cleaned_string.split(" ", 1)
    return node_type

def extract_identifier(type_str: str) -> str:
    """
    Extract the identifier from a string.
    Example: "{Function functionName at src/foo.js})" -> "functionName"
    """
    cleaned_string = type_str.strip("{}()")
    function_part, separator, file_part = cleaned_string.rpartition(" at ")
    node_type, identifier = function_part.split(" ", 1)
    return identifier

def extract_file_path(type_str: str) -> str:
    """
    Extract the file path from a string.
    Example: "{Function functionName at src/foo.js})" -> "src/foo.js"
    """
    cleaned_string = type_str.strip("{}()")
    function_part, separator, file_part = cleaned_string.rpartition(" at ")
    return file_part

def extract_commit(commit_url: str) -> str:
    """
    Extract the commit hash from a commit URL.
    Example: "https://github.com/owner/repo/commit/abc123" -> "abc123"
    """
    return commit_url.rstrip("/").split("/")[-1]

def format_before_after(type_str: str) -> str:
    """
    Format the before and after node information.
    """
    cleaned_string = type_str.strip("{}()")
    function_part, separator, file_part = cleaned_string.rpartition(" at ")
    node_type, identifier = function_part.split(" ", 1)
    return f'node("{file_part}", "{identifier}")'