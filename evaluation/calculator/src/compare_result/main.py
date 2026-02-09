import sys
import csv
from pathlib import Path
from pandas import read_csv

from src.schema import result_java_schema
from src.compare_result.join import join_table

REPO_OWNER_NAME = "icse18-refactorings"

LEFT_PATH = "./result_1015_1419_n_quote.csv"
RIGHT_PATH = "./result_1015_1952_n_quote.csv"
OUTPUT_PATH = "./compare_result.csv"

def main() -> int:
    left_path = Path(LEFT_PATH)
    try:
        if not left_path.exists():
            raise FileNotFoundError(f"File not found: {left_path}")
        left_df = read_csv(
            left_path, on_bad_lines="warn", # dtype=result_java_schema
        )
    except Exception as e:
        print(f"Error reading CSV: {e}", file=sys.stderr)
        return 2

    right_path = Path(RIGHT_PATH)
    try:
        if not right_path.exists():
            raise FileNotFoundError(f"File not found: {right_path}")
        right_df = read_csv(
            right_path, on_bad_lines="warn", # dtype=result_java_schema
        )
    except Exception as e:
        print(f"Error reading CSV: {e}", file=sys.stderr)
        return 2
    
    print(right_df.groupby("commit url").size())

    result_df = join_table(
        left_df,
        right_df,
    )

    output_path = Path(OUTPUT_PATH)
    # result_df.to_csv(output_path, index=False, encoding="utf-8", quoting=csv.QUOTE_NONNUMERIC)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
