"""Aggregate evaluation CSV by `Commit URL`
Usage:
  python3 evaluation/calculator/src/manual/extract_java_repo.py -i path/to/file.csv -o out.csv
"""

import argparse
import sys
from pathlib import Path
from typing import Optional
from urllib.parse import urlparse

import pandas as pd


def main(argv: Optional[list[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Aggregate CSV by Commit URL and count refactoring types")
    parser.add_argument("--input", "-i", default="../oracle/java/evaluation-data-public.csv", help="Input CSV file (use second line as header)")
    parser.add_argument("--output", "-o", default=None, help="Optional output CSV path; if omitted prints to stdout")
    args = parser.parse_args(argv)

    input_path = Path(args.input)
    if not input_path.exists():
        print(f"Input file not found: {input_path}", file=sys.stderr)
        return 2

    try:
        df = pd.read_csv(input_path, header=1)
    except Exception as e:
        print(f"Error reading CSV: {e}", file=sys.stderr)
        return 3

    if df.empty:
        print("Input CSV is empty", file=sys.stderr)
        return 4

    # Ensure the expected column exists
    if "Commit URL" not in df.columns:
        print("CSV does not contain 'Commit URL' column", file=sys.stderr)
        return 5

    # Extract repository from Commit URL (owner/repo)
    def extract_repo(url: str) -> Optional[str]:
        try:
            p = urlparse(url)
            parts = p.path.strip("/").split("/")
            if len(parts) >= 2:
                return f"{parts[0]}/{parts[1]}"
        except Exception:
            return None
        return None

    df["repository"] = df["Commit URL"].astype(str).apply(extract_repo)

    # Count commits per repository
    repo_counts = df.groupby("repository").size().reset_index(name="commit_rows")
    repo_counts = repo_counts.sort_values(by=["commit_rows", "repository"], ascending=[False, True])

    # Print total unique repositories
    unique_repos = repo_counts["repository"].nunique()
    print(f"Unique repositories: {unique_repos}")

    if args.output:
        try:
            repo_counts.to_csv(args.output, index=False)
        except Exception as e:
            print(f"Error writing output CSV: {e}", file=sys.stderr)
            return 6
    else:
        repo_counts.to_csv(sys.stdout, index=False)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
