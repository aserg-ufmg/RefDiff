from typing import Optional

from pandas import DataFrame, merge
from numpy import select, where

from src.schema import result_java_schema


def join_table(left_df: DataFrame, right_df: DataFrame) -> DataFrame:
    left_df = left_df.reset_index()
    right_df = right_df.reset_index()

    merge_keys = ["commit url", "relationship type", "before", "after", "expected"]
    merged_df = merge(
        left_df, # left
        right_df, # right
        on=merge_keys,
        how="outer",
        indicator=True,  # add '_merge' column (both, left_only, right_only)
    )

    result_df = DataFrame(columns=result_java_schema.keys())
    result_df["commit url"] = merged_df["commit url"]
    result_df['refactoring type'] = merged_df['refactoring type_x']
    result_df["relationship type"] = merged_df["relationship type"]
    result_df['before'] = merged_df['before']
    result_df['after'] = merged_df['after']
    result_df['expected'] = merged_df['expected']
    result_df['baseline result'] = merged_df['detected result_x']
    result_df['detected result'] = merged_df['detected result_y']
    result_df['equal to baseline'] = where(merged_df['_merge'] == 'both', merged_df['detected result_x'] == merged_df['detected result_y'], False)
    result_df['note'] = merged_df['_merge']

    return result_df
