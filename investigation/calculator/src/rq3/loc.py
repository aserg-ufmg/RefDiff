import pandas as pd
import glob
import os
import csv
import matplotlib.pyplot as plt
import datetime
import numpy as np

from src.lib.load_csv import load_csv_files
from src.lib.file_util import ensure_parent_dir

OUT_BEFORE_PATH = './output/extract_before_loc_{ts}.pdf'
OUT_AFTER_PATH = './output/extract_after_loc_{ts}.pdf'

def main():
    df_all = load_csv_files("../result")
    if df_all.empty or 'Lang' not in df_all.columns:
        print("No 'EXTRACT' refactoring data found to plot.")
        return

    extract_data_list = []
    for _, df_lang in df_all.groupby('Lang'):
        if 'RefactoringType' not in df_lang.columns:
            continue
        if 'BeforeLOC' not in df_lang.columns or 'AfterLOC' not in df_lang.columns:
            continue
        extract_df = df_lang[df_lang['RefactoringType'] == 'EXTRACT'].copy()
        extract_data_list.append(extract_df)

    if not extract_data_list:
        print("No 'EXTRACT' refactoring data found to plot.")
        return

    combined_extract_df = pd.concat(extract_data_list, ignore_index=True)

    lang_order = ['java', 'c', 'javascript', 'python', 'go', 'php', 'ruby']
    combined_extract_df['Lang'] = pd.Categorical(combined_extract_df['Lang'], categories=lang_order, ordered=True)
    combined_extract_df = combined_extract_df.sort_values('Lang')

    combined_extract_df['BeforeLOC'] = pd.to_numeric(combined_extract_df['BeforeLOC'], errors='coerce')
    combined_extract_df['AfterLOC'] = pd.to_numeric(combined_extract_df['AfterLOC'], errors='coerce')

    combined_extract_df.dropna(subset=['BeforeLOC', 'AfterLOC'], inplace=True)

    # Clip BeforeLOC to a maximum.
    # combined_extract_df['BeforeLOC'] = combined_extract_df['BeforeLOC'].clip(upper=200)

    combined_extract_df['BeforeLOC'] = combined_extract_df['BeforeLOC'].astype(int)
    combined_extract_df['AfterLOC'] = combined_extract_df['AfterLOC'].astype(int)

    if combined_extract_df.empty:
        print("No valid data to plot after cleaning.")
        return

    stats = (
        combined_extract_df
        .groupby('Lang')
        .agg(
            before_min=('BeforeLOC', 'min'),
            before_q25=('BeforeLOC', lambda s: s.quantile(0.25)),
            before_median=('BeforeLOC', 'median'),
            before_q75=('BeforeLOC', lambda s: s.quantile(0.75)),
            before_max=('BeforeLOC', 'max'),
            before_mean=('BeforeLOC', 'mean'),
            after_min=('AfterLOC', 'min'),
            after_q25=('AfterLOC', lambda s: s.quantile(0.25)),
            after_median=('AfterLOC', 'median'),
            after_q75=('AfterLOC', lambda s: s.quantile(0.75)),
            after_max=('AfterLOC', 'max'),
            after_mean=('AfterLOC', 'mean'),
        )
        .reset_index()
    )
    print("LOC summary by language (mean/median):")
    for _, row in stats.iterrows():
        print(
            f"  {row['Lang']}: "
            f"BeforeLOC mean={row['before_mean']:.2f} "
            f"min={int(row['before_min'])} q25={int(row['before_q25'])} "
            f"median={int(row['before_median'])} q75={int(row['before_q75'])} "
            f"max={int(row['before_max'])}, "
            f"AfterLOC mean={row['after_mean']:.2f} "
            f"min={int(row['after_min'])} q25={int(row['after_q25'])} "
            f"median={int(row['after_median'])} q75={int(row['after_q75'])} "
            f"max={int(row['after_max'])}"
        )

    plt.style.use('ggplot')

    # BeforeLOC boxplot
    fig1, ax1 = plt.subplots(figsize=(12, 8))
    before_order = (
        combined_extract_df
        .groupby('Lang', observed=False)['BeforeLOC']
        .median()
        .sort_values(ascending=False)
        .index
        .tolist()
    )
    df_before = combined_extract_df.copy()
    df_before['Lang'] = pd.Categorical(df_before['Lang'], categories=before_order, ordered=True)
    df_before.boxplot(column='BeforeLOC', by='Lang', ax=ax1, showfliers=False, showmeans=True)
    ax1.set_title('LOC for EXTRACT Refactoring by Language')
    ax1.set_xlabel('Language')
    ax1.set_ylabel('Lines of Code')
    plt.suptitle('')
    plt.tight_layout()
    out_before_path = OUT_BEFORE_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
    ensure_parent_dir(out_before_path)
    plt.savefig(out_before_path)
    print(f"Saved BeforeLOC boxplot to {out_before_path}")
    plt.close(fig1)

    # AfterLOC boxplot
    fig2, ax2 = plt.subplots(figsize=(12, 8))
    after_order = (
        combined_extract_df
        .groupby('Lang', observed=False)['AfterLOC']
        .median()
        .sort_values(ascending=False)
        .index
        .tolist()
    )
    df_after = combined_extract_df.copy()
    df_after['Lang'] = pd.Categorical(df_after['Lang'], categories=after_order, ordered=True)
    df_after.boxplot(column='AfterLOC', by='Lang', ax=ax2, showfliers=False, showmeans=True)
    ax2.set_title('LOC for EXTRACT Refactoring by Language')
    ax2.set_xlabel('Language')
    ax2.set_ylabel('Lines of Code')
    plt.suptitle('')
    plt.tight_layout()
    out_after_path = OUT_AFTER_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
    ensure_parent_dir(out_after_path)
    plt.savefig(out_after_path)
    print(f"Saved AfterLOC boxplot to {out_after_path}")
    plt.close(fig2)


if __name__ == '__main__':
    main()
