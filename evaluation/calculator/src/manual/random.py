"""Select random records from a CSV and print or save the sample.

Defaults are defined as constants at the top of the file.
"""

import argparse
import sys
from pathlib import Path
from typing import Optional

from pandas import read_csv, DataFrame, concat

# Default CLI values (use these constants in argparse)
DEFAULT_DIR = "../detection-result/c/head"
# DEFAULT_DIR = "../detection-result/javascript/head"
# Default seed used when none is provided explicitly
DEFAULT_SEED = 42
# Number of records to sample per `type` value when grouping by 'type'
SAMPLES_PER_TYPE = 10
# Default output directory (parent of evaluation/calculator/src is evaluation/calculator/,
# so ../sampled matches request)
DEFAULT_OUTPUT_DIR = "./sampled"

def main(argv: Optional[list[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Select random records from CSVs inside a single directory")
    parser.add_argument("--dir", "-d", default=DEFAULT_DIR, help="Path to directory containing CSV files")
    parser.add_argument("--per-type", type=int, default=SAMPLES_PER_TYPE, help="Number of records to sample per 'type' value")
    parser.add_argument("--seed", type=int, default=DEFAULT_SEED, help="Optional random seed")
    parser.add_argument("--output", "-o", default=None, help="Optional output CSV path (if omitted, writes to DEFAULT_OUTPUT_DIR/sampled-{input filename})")
    args = parser.parse_args(argv)

    # Support single directory input: validate directory and collect CSV files (non-recursive)
    dir_path = Path(args.dir)
    if not dir_path.exists():
        print(f"Path not found: {dir_path}", file=sys.stderr)
        return 2
    if not dir_path.is_dir():
        print(f"Not a directory: {dir_path}", file=sys.stderr)
        return 2
    # collect .csv files in the directory (non-recursive), deterministic order
    expanded_files = sorted([f for f in dir_path.iterdir() if f.is_file() and f.suffix.lower() == ".csv"])
    if not expanded_files:
        print(f"No CSV files found in directory: {dir_path}", file=sys.stderr)
        return 2

    data_frames: list[DataFrame] = []
    for p in expanded_files:
        try:
            df_part = read_csv(p, on_bad_lines="warn")
        except Exception as e:
            print(f"Error reading CSV {p}: {e}", file=sys.stderr)
            return 3
        data_frames.append(df_part)

    # Concatenate all input files into a single DataFrame
    try:
        df = concat(data_frames, ignore_index=True)
    except Exception as e:
        print(f"Error concatenating input CSVs: {e}", file=sys.stderr)
        return 3

    if df.empty:
        print("Input CSV is empty", file=sys.stderr)
        return 4

    # If the CSV has a 'type' column, sample per-type; otherwise fall back to global sampling
    per_type = int(args.per_type)
    try:
        # extract syntax element from the start of the 'before' column (e.g. '{Function', 'Class')
        if "before" in df.columns:
            df["element"] = df["before"].astype(str).str.extract(r'^\{?\s*([A-Za-z_]+)', expand=False)
        else:
            df["element"] = ""

        if "type" not in df.columns:
            print("CSV does not contain a 'type' column; per-type sampling requires 'type'", file=sys.stderr)
            return 5

        # Sample per-type while ensuring no two sampled rows share the same 'commit'
        if "commit" not in df.columns:
            print("CSV does not contain a 'commit' column; uniqueness across commits requires 'commit'", file=sys.stderr)
            return 7

        parts = []
        chosen_commits: set[str] = set()
        # iterate groups in file order (avoid sorting groups)
        for _, g in df.groupby("type", sort=False):
            # iterate element categories in order of appearance within this type
            seen_elements: list[str] = []
            for val in g["element"].fillna("").tolist():
                if val not in seen_elements:
                    seen_elements.append(val)
            for element in seen_elements:
                # exclude rows whose commit is already chosen and match the element
                available = g[(g["element"] == element) & (~g["commit"].isin(chosen_commits))]
                if available.empty:
                    continue
                k = min(len(available), per_type)
                if args.seed is not None:
                    sampled_part = available.sample(n=k, random_state=int(args.seed))
                else:
                    sampled_part = available.sample(n=k)
                parts.append(sampled_part)
                # record chosen commits to avoid duplicates across groups
                chosen_commits.update(sampled_part["commit"].astype(str).tolist())

        if parts:
            sampled = concat(parts)
            # sort output by 'type' so rows are grouped by type in the CSV
            sampled = sampled.sort_values(by="type")
        else:
            sampled = df.head(0)

        for _col in ("manual check result", "notes"):
            if _col not in sampled.columns:
                sampled[_col] = ""
    except Exception as e:
        print(f"Error sampling CSV by type: {e}", file=sys.stderr)
        return 6

    # Determine output path: use provided --output, otherwise default to DEFAULT_OUTPUT_DIR/sampled-{input filename}
    output_path = make_output_path(dir_path, expanded_files, args.seed, args.output)

    try:
        sampled.to_csv(output_path, index=False)
    except Exception as e:
        print(f"Error writing output CSV: {e}", file=sys.stderr)
        return 6

    # Also print the chosen output path to stdout for convenience
    print(f"Wrote {len(sampled)} records to: {output_path}")

    return 0

def make_output_path(
    dir_path: Path,
    expanded_files: list[Path],
    seed: Optional[int] = None,
    output: Optional[str] = None,
) -> Path:
    """Generate the output CSV path.

    If `output` is provided, return that Path. Otherwise create `DEFAULT_OUTPUT_DIR`
    and generate a filename using the language extracted from `dir_path` when
    possible (e.g. detection-result/javascript/head -> javascript). Falls back
    to directory or first-file based naming.
    """
    if output:
        return Path(output)

    output_dir = Path(DEFAULT_OUTPUT_DIR)
    output_dir.mkdir(parents=True, exist_ok=True)

    # Derive language name from directory path if possible
    language = None
    parts = dir_path.parts
    if "detection-result" in parts:
        try:
            idx = parts.index("detection-result")
            if idx + 1 < len(parts):
                language = parts[idx + 1]
        except ValueError:
            language = None

    # fallback: if dir ends with 'head', use parent folder name
    if language is None and dir_path.name == "head" and dir_path.parent:
        language = dir_path.parent.name

    # Choose base name: prefer extracted language, else fall back
    if language:
        base = language
    else:
        if len(expanded_files) == 1:
            base = expanded_files[0].stem
        else:
            base = dir_path.name

    ext = expanded_files[0].suffix or ".csv"
    filename = f"sampled-{base}"
    if seed is not None:
        filename += f"-seed{seed}"
    filename += ext

    return output_dir / filename

if __name__ == "__main__":
	raise SystemExit(main())
