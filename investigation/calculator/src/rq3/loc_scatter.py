import pandas as pd
import matplotlib.pyplot as plt
import datetime

from src.lib.load_csv import load_csv_files
from src.lib.file_util import ensure_parent_dir

OUT_BEFORE_PATH = './output/extract_loc_scatter_before_{ts}.pdf'
OUT_AFTER_PATH = './output/extract_loc_scatter_after_{ts}.pdf'

FREQUENT_LOC_THRESHOLD = 500
OVER_LOC_THRESHOLD = 2000


def build_loc_counts(df, loc_col):
    series = pd.to_numeric(df[loc_col], errors='coerce').dropna().astype(int)
    series = series[series >= 0]
    return series.value_counts().sort_index()


def report_frequent_loc_commits(df, loc_col, threshold):
    loc_series = pd.to_numeric(df[loc_col], errors='coerce').dropna().astype(int)
    loc_series = loc_series[loc_series >= 0]
    if loc_series.empty:
        return

    counts = loc_series.value_counts()
    frequent_locs = counts[counts >= threshold]
    if frequent_locs.empty:
        return

    for loc_value, total in frequent_locs.sort_index().items():
        subset = df.loc[loc_series.index].copy()
        subset = subset[subset[loc_col].astype(str) == str(loc_value)]
        if "Commit" not in subset.columns:
            print(f"{loc_col}={loc_value} count={int(total)} (Commit column missing)")
            continue
        commit_counts = subset["Commit"].astype(str)
        commit_counts = commit_counts[commit_counts != ""].value_counts()
        commit_counts = commit_counts[commit_counts > 10]
        print(f"{loc_col}={loc_value} count={int(total)} unique_commits={int(commit_counts.shape[0])}")
        subset = subset[subset["Commit"].astype(str).isin(commit_counts.index)]
        for commit_hash, cnt in commit_counts.sort_values(ascending=True).items():
            row = subset[subset["Commit"].astype(str) == commit_hash].head(1)
            lang = row["Lang"].iloc[0] if "Lang" in row.columns and not row.empty else ""
            repo = row.get("RepoName", row.get("Repository", row.get("Repo", ""))).iloc[0] if not row.empty else ""
            lang_str = f" lang={lang}" if pd.notna(lang) and str(lang) != "" else ""
            repo_str = f" repo={repo}" if pd.notna(repo) and str(repo) != "" else ""
            print(f"  {commit_hash}: {int(cnt)}{lang_str}{repo_str}")


def report_over_loc_commits(df, loc_col, threshold):
    if "Commit" not in df.columns:
        return
    loc_series = pd.to_numeric(df[loc_col], errors='coerce')
    over_mask = loc_series > threshold
    if not over_mask.any():
        return

    cols = ["Commit", loc_col]
    if "Lang" in df.columns:
        cols.append("Lang")
    if "RepoName" in df.columns:
        cols.append("RepoName")
    elif "Repository" in df.columns:
        cols.append("Repository")
    elif "Repo" in df.columns:
        cols.append("Repo")

    subset = df.loc[over_mask, cols].copy()
    subset["Commit"] = subset["Commit"].astype(str).str.strip()
    subset = subset[subset["Commit"] != ""]
    if subset.empty:
        return

    commit_counts = subset["Commit"].value_counts()
    multi_commit = commit_counts[commit_counts > 1].index
    subset = subset[subset["Commit"].isin(multi_commit)]
    if subset.empty:
        return

    subset = subset.sort_values(loc_col, ascending=False).drop_duplicates(subset=["Commit"])
    print(f"{loc_col} > {threshold} commits:")
    for _, row in subset.iterrows():
        lang = row.get("Lang", "")
        repo = row.get("RepoName", row.get("Repository", row.get("Repo", "")))
        lang_str = f" lang={lang}" if pd.notna(lang) and str(lang) != "" else ""
        repo_str = f" repo={repo}" if pd.notna(repo) and str(repo) != "" else ""
        print(f"  {row['Commit']} ({loc_col}={int(row[loc_col])}{lang_str}{repo_str})")


if __name__ == '__main__':
    df_all = load_csv_files("../result")

    if df_all.empty or "Lang" not in df_all.columns:
        print("No CSV data found to plot.")
        raise SystemExit(0)

    df = df_all.copy()

    if "RefactoringType" in df.columns:
        df = df[df["RefactoringType"] == "EXTRACT"].copy()

    if df.empty:
        print("No 'EXTRACT' refactoring data found to plot.")
        raise SystemExit(0)

    report_frequent_loc_commits(df, "BeforeLOC", FREQUENT_LOC_THRESHOLD)
    report_frequent_loc_commits(df, "AfterLOC", FREQUENT_LOC_THRESHOLD)
    report_over_loc_commits(df, "BeforeLOC", OVER_LOC_THRESHOLD)
    report_over_loc_commits(df, "AfterLOC", OVER_LOC_THRESHOLD)

    plt.style.use('ggplot')

    fig_before, ax_before = plt.subplots(figsize=(12, 8))
    fig_after, ax_after = plt.subplots(figsize=(12, 8))

    plotted_before = False
    plotted_after = False
    for lang in sorted(df["Lang"].dropna().astype(str).unique()):
        df_lang = df[df["Lang"].astype(str) == lang]
        counts_before = build_loc_counts(df_lang, "BeforeLOC")
        counts_after = build_loc_counts(df_lang, "AfterLOC")
        if not counts_before.empty:
            ax_before.scatter(counts_before.index, counts_before.values, label=lang, s=18, alpha=0.8)
            plotted_before = True
        if not counts_after.empty:
            ax_after.scatter(counts_after.index, counts_after.values, label=lang, s=18, alpha=0.8)
            plotted_after = True

    if not plotted_before and not plotted_after:
        print("No valid LOC values to plot.")
        raise SystemExit(0)

    if plotted_before:
        ax_before.set_title("EXTRACT Refactoring LOC Distribution (Before)")
        ax_before.set_xlabel("Lines of Code")
        ax_before.set_ylabel("Count")
        ax_before.legend()
        plt.tight_layout()
        out_before_path = OUT_BEFORE_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
        ensure_parent_dir(out_before_path)
        fig_before.savefig(out_before_path)
        print(f"Saved LOC before scatter plot to {out_before_path}")
    plt.close(fig_before)

    if plotted_after:
        ax_after.set_title("EXTRACT Refactoring LOC Distribution (After)")
        ax_after.set_xlabel("Lines of Code")
        ax_after.set_ylabel("Count")
        ax_after.legend()
        plt.tight_layout()
        out_after_path = OUT_AFTER_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
        ensure_parent_dir(out_after_path)
        fig_after.savefig(out_after_path)
        print(f"Saved LOC after scatter plot to {out_after_path}")
    plt.close(fig_after)
