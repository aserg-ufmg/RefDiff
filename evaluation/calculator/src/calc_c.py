from pandas import DataFrame
from typing import Dict, Any
import math

from src.calc_util import calc_precision, calc_recall

def calc_c_precision_recall(
    result_df: DataFrame,
) -> Dict[str, Any]:
    count = (
        result_df.groupby("type")["detected result"]
        .value_counts()
        .unstack(fill_value=0.0)
        .to_dict(orient="index")
    )

    total_count = {"TP": 0.0, "FP": 0.0, "FN": 0.0, "TN": 0.0}
    for _, v in count.items():
        total_count["TP"] += v.get("TP", 0)
        total_count["FP"] += v.get("FP", 0)
        total_count["FN"] += v.get("FN", 0)
        total_count["TN"] += v.get("TN", 0)
    count["Total"] = total_count

    for _, v in count.items():
        tp = v.get("TP", 0)
        fp = v.get("FP", 0)
        fn = v.get("FN", 0)
        v["precision"] = calc_precision(tp=tp, fp=fp)
        v["recall"] = calc_recall(tp=tp, fn=fn)

    return count

    # return {
    #     key: count[key]
    #     for key in [ # Order types according to the table in the paper.
    #         "CHANGE_SIGNATURE",
    #         "MOVE_FILE",
    #         "MOVE_FUNCTION",
    #         "RENAME_FILE",
    #         "RENAME_FUNCTION",
    #         "MOVE_RENAME_FILE",
    #         "MOVE_RENAME_FUNCTION",
    #         "EXTRACT",
    #         "INLINE",
    #         "Total"
    #     ]
    # }
