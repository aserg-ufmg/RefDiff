from typing import Optional

from pandas import DataFrame, merge
from numpy import select

from src.schema import result_c_schema

REPO_OWNER = "refdiff-study"

def join_table_c_precision(oracle_df: DataFrame, detected_df: DataFrame, does_ignore_line: Optional[bool]=True) -> DataFrame:
    oracle_df = oracle_df.reset_index()
    detected_df = detected_df.reset_index()

    oracle_df = oracle_df.rename(
        columns={"Type": "type", "Node Before": "before", "Node After": "after", "index": "oracle index"}
    )
    detected_df = detected_df.rename(
        columns={"index": "detected index"}
    )

    detected_df["Commit URL"] = detected_df.apply(
        lambda row: create_commit_url(REPO_OWNER, row["repository"], row["commit"]), axis=1
    )

    detected_df["type"] = detected_df.apply(
        lambda row: format_type(row["type"], row["before"]), axis=1
    )

    oracle_df["before"] = oracle_df.apply(
        lambda row: format_oracle_before_after(row["before"]), axis=1
    )
    oracle_df["after"] = oracle_df.apply(
        lambda row: format_oracle_before_after(row["after"]), axis=1
    )
    detected_df["before"] = detected_df.apply(
        lambda row: format_detected_before_after(row["before"]), axis=1
    )
    detected_df["after"] = detected_df.apply(
        lambda row: format_detected_before_after(row["after"]), axis=1
    )

    merge_keys = ["Commit URL", "type", "before", "after"]
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


def join_table_c_recall(
    oracle_df: DataFrame,
    detected_df: DataFrame,
    does_ignore_line: Optional[bool] = True,
) -> DataFrame:
    oracle_df = oracle_df.reset_index()
    detected_df = detected_df.reset_index()

    oracle_df = oracle_df.rename(
        columns={
            "Type": "type",
            "Commit": "commit",
            "Node Before": "before",
            "Node After": "after",
            "index": "oracle index",
        }
    )
    detected_df = detected_df.rename(columns={"index": "detected index"})

    detected_df["type"] = detected_df.apply(
        lambda row: format_type(row["type"], row["before"]), axis=1
    )

    # TODO Every owner isn't refdiff-study for recall
    # detected_df["Commit URL"] = detected_df.apply(
    #     lambda row: create_commit_url(REPO_OWNER, row["repository"], row["commit"]),
    #     axis=1,
    # )

    oracle_df["before"] = oracle_df.apply(
        lambda row: format_oracle_before_after(row["before"], row["type"]), axis=1
    )
    oracle_df["after"] = oracle_df.apply(
        lambda row: format_oracle_before_after(row["after"], row["type"]), axis=1
    )
    detected_df["before"] = detected_df.apply(
        lambda row: format_detected_before_after(row["before"]), axis=1
    )
    detected_df["after"] = detected_df.apply(
        lambda row: format_detected_before_after(row["after"]), axis=1
    )

    merge_keys = ["commit", "type", "before", "after"]
    merged_df = merge(
        oracle_df,  # left
        detected_df,  # right
        on=merge_keys,
        how="left",
        # how="outer",
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
    # result_df["commit url"] = merged_df["Commit URL"]
    result_df["commit url"] = merged_df["commit"]
    result_df["type"] = merged_df["type"]
    result_df["before"] = merged_df["before"]
    result_df["after"] = merged_df["after"]
    result_df["baseline result"] = merged_df["Detected"]
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

def format_type(type: str, before: str) -> str:
    if type == "MOVE" and before.startswith("{File"):
        return f"MOVE_FILE"
    elif type == "MOVE" and before.startswith("{Function"):
        return f"MOVE_FUNCTION"
    elif type == "RENAME" and before.startswith("{File"):
        return f"RENAME_FILE"
    elif type == "RENAME" and before.startswith("{Function"):
        return f"RENAME_FUNCTION"
    elif type == "MOVE_RENAME" and before.startswith("{File"):
        return f"MOVE_RENAME_FILE"
    elif type == "MOVE_RENAME" and before.startswith("{Function"):
        return f"MOVE_RENAME_FUNCTION"
    else:
        return type

def format_detected_before_after(node: str) -> str:
    """
        Format the before and after nodes for output.
        Input example: "{Function oldFunctionName(param1, param2) at src/foo.c:123})"
    """
    if node.startswith("{File"):
        # Remove surrounding brackets and parentheses
        cleaned_string = node.strip("{}()")

        # Split the string at the last " at "
        function_part, separator, file_part = cleaned_string.rpartition(" at ")

        file_part = file_part.split(":")[0]

        # Combine into the final format
        result = f"{file_part}"
        return result
    elif node.startswith("{Function"):
        # Remove surrounding brackets and parentheses
        cleaned_string = node.strip("{}()")

        # Split the string at the last " at "
        function_part, separator, file_part = cleaned_string.rpartition(" at ")

        # Remove either "Function " from the beginning
        # Chain .replace() to handle both cases cleanly.
        function_call = function_part.replace("Function ", "")

        function_name = function_call.split('(')[0]

        file_part = file_part.split(":")[0]

        # Combine into the final format
        result = f"{file_part}:{function_name}"
        return result

def format_oracle_before_after(node: str, type: str) -> str:
    """
    Format the before and after nodes from oracle for output.
    Input examples:
        - "src/network/Server.c:swServer_call_hook_func(swServer, swServer_hook_type):30696-30997", _FUNCTION
        - "ext/mbstring/mbstring.c:php_mb_chr(long, char)", _FUNCTION
        - "arch/arm/plat-omap/dmtimer.c", _FILE
    """
    if type.endswith("_FILE"):
        return node
    elif type.endswith("_FUNCTION"):
        # Split the input into file path and function details
        file_part, _, function_part = node.partition(":")

        # Extract the function name (ignore parameters and line numbers if present)
        function_name = function_part.split("(")[0]

        # Combine the file path and function name
        result = f"{file_part}:{function_name}"
        return result
