"""Read and show summary for a java.csv file produced by the evaluation.

Default path: ../detection-result/1011-1833/java.csv

Usage:
  python -m src.main --file ../detection-result/1011-1833/java.csv
"""

import argparse
import sys
import csv
from pathlib import Path
from pprint import pprint

from pandas import read_csv, DataFrame

from src.schema import (
    oracle_java_schema,
    oracle_c_precision_schema,
    detected_schema,
    oracle_c_recall_schema,
    oracle_js_precision_schema,
    oracle_js_recall_schema,
)
from src.join_c import join_table_c_precision, join_table_c_recall
from src.join_js import join_table_js_precision, join_table_js_recall
from src.join_java import join_table_java
from src.calc_c import calc_c_precision_recall
from src.calc_java import calc_java_precision_recall
from src.calc_js import calc_js_precision_recall

def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Read and summarize a java.csv result file")
    parser.add_argument("--language", "-l", default="java", choices=["java", "c", "js"], help="Programming language")
    parser.add_argument("--metric", "-m", default="precision", choices=["precision", "recall"], help="Metric to calculate")
    # parser.add_argument("--detected", "-d", default="../detection-result/java/java-0121-0453.csv", help="Relative path to detected results")
    parser.add_argument("--detected", "-d", default="../detection-result/javascript/recall/javascript-recall-0121-0533.csv", help="Relative path to detected results")
    args = parser.parse_args(argv)

    detected_path = Path(args.detected)
    try:
        if not detected_path.exists():
            raise FileNotFoundError(f"File not found: {detected_path}")
        detected_df = read_csv(
            detected_path, on_bad_lines="warn", dtype=detected_schema
        )
    except Exception as e:
        print(f"Error reading CSV: {e}", file=sys.stderr)
        return 2
    detected_df = detected_df.dropna() # drop rows containing "Error processing commit" in csv
    oracle_df = read_oracle_csv(args.language, args.metric)
    result_df = join_table(oracle_df, detected_df, args.language, args.metric)
    # result_df = join_table(oracle_java_df, detected_reprod_java_df, REPO_OWNER_NAME, does_ignore_line=False)

    output_path = Path(f"result/{extract_filename(Path(args.detected))}-joined.csv")
    result_df["oracle index"] = result_df["oracle index"].astype('Int64')
    result_df["detected index"] = result_df["detected index"].astype('Int64')
    result_df.to_csv(output_path, index=False, encoding="utf-8", quoting=csv.QUOTE_NONNUMERIC)

    try:
        metrics = calc_metrics(result_df, args.language)
        pprint(metrics, sort_dicts=False)
        output_path = Path(f"result/{extract_filename(Path(args.detected))}-summary.txt")
        with open(output_path, "w", encoding="utf-8") as f:
            for ref_type, vals in metrics.items():
                f.write(f"{ref_type}:\n")
                f.write(f"  TP: {vals.get('TP', 0)}\n")
                f.write(f"  FP: {vals.get('FP', 0)}\n")
                f.write(f"  FN: {vals.get('FN', 0)}\n")
                f.write(f"  TN: {vals.get('TN', 0)}\n")
                f.write(f"  Precision: {vals.get('precision', 0):.4f}\n")
                f.write(f"  Recall: {vals.get('recall', 0):.4f}\n\n")
    except Exception as e:
        print(f"Error calculating precision/recall: {e}", file=sys.stderr)
    return 0

def read_oracle_csv(language: str, metric: str) -> DataFrame:
    if language == "java":
        path = Path("../oracle/java/evaluation-data-public.csv")
        schema = oracle_java_schema
        return read_csv(
            path, on_bad_lines="warn", dtype=schema, skiprows=[0] # skip first row (header description)
        )
    elif language == "c" and metric == "precision":
        path = Path("../oracle/c/c-analyzed-precision.csv")
        schema = oracle_c_precision_schema
        return read_csv(
            path, on_bad_lines="warn", dtype=schema
        )
    elif language == "c" and metric == "recall":
        path = Path("../oracle/c/c-documented-recall.csv")
        schema = oracle_c_recall_schema
        return read_csv(
            path, on_bad_lines="warn", dtype=schema
        )
    elif language == "js" and metric == "precision":
        path = Path("../oracle/js/js-analyzed-precision.csv")
        schema = oracle_js_precision_schema
        return read_csv(
            path, on_bad_lines="warn", dtype=schema
        )
    elif language == "js" and metric == "recall":
        path = Path("../oracle/js/js-documented-recall.csv")
        schema = oracle_js_recall_schema
        return read_csv(
            path, on_bad_lines="warn", dtype=schema
        )
    else:
        raise ValueError(f"Unsupported language: {language}")

def join_table(oracle_df: DataFrame, detected_df: DataFrame, language: str, metric: str) -> DataFrame:
    if language == "java":
        return join_table_java(oracle_df, detected_df)
    elif language == "c" and metric == "precision":
        return join_table_c_precision(oracle_df, detected_df)
    elif language == "c" and metric == "recall":
        return join_table_c_recall(oracle_df, detected_df)
    elif language == "js" and metric == "precision":
        return join_table_js_precision(oracle_df, detected_df)
    elif language == "js" and metric == "recall":
        return join_table_js_recall(oracle_df, detected_df)
    else:
        raise ValueError(f"Unsupported language: {language}")

def calc_metrics(result_df: DataFrame, language: str) -> dict[str, dict[str, float]]:
    if language == "java":
        return calc_java_precision_recall(result_df)
    elif language == "c":
        return calc_c_precision_recall(result_df)
    elif language == "js":
        return calc_js_precision_recall(result_df)
    else:
        raise ValueError(f"Unsupported language: {language}")

def extract_filename(path: Path) -> str:
    return path.stem.split(".")[0]


if __name__ == "__main__":
    raise SystemExit(main())
