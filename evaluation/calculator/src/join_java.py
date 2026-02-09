from typing import Optional

from pandas import DataFrame, merge
from numpy import select, where

from src.schema import result_java_schema

REPO_OWNER = "icse18-refactorings"

def join_table_java(oracle_df: DataFrame, detected_df: DataFrame, does_ignore_line: Optional[bool]=True) -> DataFrame:
    oracle_df = oracle_df.reset_index()
    detected_df = detected_df.reset_index()

    oracle_df = oracle_df.rename(
        columns={"CST Node before": "before", "CST Node After": "after", "index": "oracle index"}
    )
    detected_df = detected_df.rename(
        columns={"index": "detected index"}
    )

    detected_df["Commit URL"] = detected_df.apply(
        lambda row: create_commit_url(REPO_OWNER, row["repository"], row["commit"]), axis=1
    )

    if does_ignore_line:
        # Normalize 'before' and 'after' columns by stripping whitespace and removing line numbers (e.g., ":123" at the end).
        # Because line numbers may differ between oracle and detected results depending on inclusions of comments and blank lines.
        oracle_df["before"] = oracle_df["before"].str.replace(r":\d+", "", regex=True)
        oracle_df["after"] = oracle_df["after"].str.replace(r":\d+", "", regex=True)
        detected_df["before"] = detected_df["before"].str.replace(r":\d+", "", regex=True)
        detected_df["after"] = detected_df["after"].str.replace(r":\d+", "", regex=True)

    oracle_df["join_key_type"] = oracle_df["Relationship Type"].str.upper()
    detected_df["join_key_type"] = detected_df["type"].str.upper()

    merge_keys = ["Commit URL", "join_key_type", "before", "after"]
    merged_df = merge(
        oracle_df, # left
        detected_df, # right
        on=merge_keys,
        how="outer",
        indicator=True,  # add '_merge' column (both, left_only, right_only)
    )

    conditions = [
        (merged_df["_merge"] == "both") & (merged_df["Expected?"] == "T"),
        (merged_df["_merge"] == "both") & (merged_df["Expected?"] == "F"),
        (merged_df["_merge"] == "right_only"), # detected but not expected
        (merged_df["_merge"] == "left_only") & (merged_df["Expected?"] == "F"),
        (merged_df["_merge"] == "left_only") & (merged_df["Expected?"] == "T"),
    ]
    choices = ["TP", "FP", "FP", "TN", "FN"]
    merged_df["detected result"] = select(
        conditions, choices, default="Unknown"
    )

    result_df = DataFrame(columns=result_java_schema.keys())
    result_df["commit url"] = merged_df["Commit URL"]
    result_df['refactoring type'] = merged_df['Refactoring Type']
    result_df["relationship type"] = where(merged_df['_merge'] != 'right_only', merged_df['Relationship Type'], merged_df['type'])
    result_df['before'] = merged_df['before']
    result_df['after'] = merged_df['after']
    result_df["expected"] = merged_df['Expected?']
    result_df["baseline result"] = merged_df["RefDiff 2.0"]
    result_df["detected result"] = merged_df[
        "detected result"
    ]
    result_df["equal to baseline"] = result_df["baseline result"] == result_df[
        "detected result"
    ]

    result_df["oracle index"] = merged_df["oracle index"]
    result_df["detected index"] = merged_df["detected index"]
    result_df["note"] = ""
    # Mark rows that are detected-only (not present in the dataset/oracle)
    try:
        mask_right_only = merged_df["_merge"] == "right_only"
        result_df.loc[mask_right_only, "note"] = "Not in dataset"
    except Exception:
        # if _merge column missing or assignment fails, leave notes empty
        pass

    return result_df

def create_commit_url(owner_name: str, repo_name: str, commit: str) -> str:
    return f"https://github.com/{owner_name}/{repo_name}/commit/{commit}"
