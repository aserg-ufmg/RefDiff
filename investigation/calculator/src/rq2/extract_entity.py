import pandas as pd

from src.lib.load_csv import load_csv_files

if __name__ == "__main__":
    df = load_csv_files("../result")

    if "Lang" not in df.columns or "Before" not in df.columns:
        print("Required columns (Lang, Before) are missing.")
        exit(1)

    for lang, df_lang in df.groupby("Lang"):
        before_values = (
            df_lang["Before"]
            .dropna()
            .astype(str)
            .str.strip()
            .str.lstrip("{")
        )
        first_words = before_values.str.split().str[0].dropna()
        counts = first_words.value_counts()

        print(f"[{lang}]")
        for word, count in counts.items():
            print(f"{word}\t{count}")
        print()
