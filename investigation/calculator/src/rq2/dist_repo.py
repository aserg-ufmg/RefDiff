import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
import datetime
import numpy as np
import re

from src.lib.load_csv import load_csv_files
from src.lib.file_util import ensure_parent_dir

OUT_PATH_TEMPLATE = './output/refactoring_dist_repo_{ts}.pdf'

ENTITY_NAMES_BY_LANG = {
    "java": ["Method"],
    "c": ["Function"],
    "javascript": ["Function"],
    "python": ["Function"],
    "go": ["function_declaration", "method_declaration"],
    "php": ["function_definition", "method_declaration"],
    "ruby": ["Method"],
}

if __name__ == '__main__':
    df = load_csv_files("../result")

    # Step 1: Calculate percentages
    all_counts = {}
    allowed_refactoring_types = ['CHANGE_SIGNATURE', 'EXTRACT', 'EXTRACT_MOVE', 'INLINE', 'MOVE', 'RENAME', 'MOVE_RENAME']

    for repo, df_repo in df.groupby('Repo'):
        df_repo = df_repo.copy()
        if 'RefactoringType' in df_repo.columns:
            if 'Before' in df_repo.columns:
                lang = df_repo['Lang'].iloc[0] if 'Lang' in df_repo.columns and not df_repo.empty else None
                entity_names = ENTITY_NAMES_BY_LANG.get(lang, []) if lang is not None else []
                if entity_names:
                    pattern = "|".join(re.escape(name) for name in entity_names)
                    df_repo = df_repo[df_repo['Before'].astype(str).str.contains(pattern, na=False)]
                else:
                    df_repo = df_repo.iloc[0:0]

            df_repo['RefactoringType'] = df_repo['RefactoringType'].replace(['INTERNAL_MOVE'], 'MOVE')
            df_repo['RefactoringType'] = df_repo['RefactoringType'].replace(['INTERNAL_MOVE_RENAME'], 'MOVE_RENAME')

            # Filter to allowed refactoring types
            filtered_df = df_repo[df_repo['RefactoringType'].isin(allowed_refactoring_types)]

            # Percentages per repo
            counts = filtered_df['RefactoringType'].value_counts(normalize=True) * 100
            all_counts[repo] = counts


    # Prepare data for plotting
    plot_data = pd.DataFrame(all_counts).fillna(0)

    # Check if there is data to plot
    if plot_data.empty:
        print("No data available to plot.")
        exit(0)

    # order repos by language then repo name
    repo_lang = df[['Repo', 'Lang']].drop_duplicates().set_index('Repo')['Lang'].to_dict()
    lang_order = ['java', 'c', 'javascript', 'python', 'go', 'php', 'ruby']
    lang_rank = {lang: i for i, lang in enumerate(lang_order)}
    ordered_repos = sorted(repo_lang.keys(), key=lambda r: (lang_rank.get(repo_lang.get(r, ''), 999), r))

    # Rows: repos (ordered_repos), Columns: refactoring types
    plot_df = plot_data.reindex(columns=ordered_repos).T.fillna(0)

    # Base x positions with extra gap between language groups
    base_positions = []
    x = 0.0
    gap = 0.6
    for i, repo in enumerate(ordered_repos):
        base_positions.append(x)
        if i < len(ordered_repos) - 1 and repo_lang.get(ordered_repos[i + 1]) != repo_lang.get(repo):
            x += 1 + gap
        else:
            x += 1

    n_types = len(plot_df.columns)
    if n_types == 0:
        print("No refactoring types to plot.")
        exit(0)

    fig, ax = plt.subplots(figsize=(28, 10))
    ax.tick_params(axis='both', labelsize=14)

    # Stack bars manually for each repo
    bottoms = np.zeros(len(ordered_repos))
    width = 0.8
    for j, ref_type in enumerate(plot_df.columns):
        heights = plot_df[ref_type].values
        bars = ax.bar(base_positions, heights, bottom=bottoms, width=width, label=ref_type)

        # annotate with percentage inside each stacked segment
        for rect, percentage in zip(bars, heights):
            if percentage > 1:  # Use a threshold to avoid clutter
                ax.text(rect.get_x() + rect.get_width() / 2,
                        rect.get_y() + rect.get_height() / 2,
                        f"{percentage:.1f}", ha='center', va='center', color='white', fontweight='bold', fontsize=10)

        bottoms += heights

    # tighten x-limits without changing bar width
    if base_positions:
        pad = 0.2
        ax.set_xlim(min(base_positions) - width / 2 - pad,
                    max(base_positions) + width / 2 + pad)

    # set x tick labels at base positions
    labels = [f"{repo}\n({repo_lang.get(repo,'')})" for repo in ordered_repos]
    ax.set_xticks(base_positions)
    ax.set_xticklabels(labels, rotation=60, ha='center')

    plt.title('Distribution of Refactoring Types by Project', fontsize=18)
    plt.xlabel('Project (language in parenthesis)', fontsize=16)
    plt.ylabel('Percentage (%)', fontsize=16)

    # Place legend as a single horizontal row below the plot (use figure legend to avoid clipping)
    ncol = max(1, len(plot_df.columns))
    handles, labels = ax.get_legend_handles_labels()
    fig.legend(handles, labels, title='Refactoring Type', ncol=ncol,
               fontsize=14, title_fontsize=15,
               loc='lower center', bbox_to_anchor=(0.5, -0.02), bbox_transform=fig.transFigure)
    # Make room at the bottom for the legend and keep it from clipping
    plt.subplots_adjust(bottom=0.30)
    plt.tight_layout(rect=[0, 0.12, 1, 1])

    # Save the plot (include month-day and hour-minute, no year)
    out_path = OUT_PATH_TEMPLATE.format(ts=datetime.datetime.now().strftime("%m-%d_%H-%M"))
    ensure_parent_dir(out_path)
    plt.savefig(out_path, bbox_inches='tight')

    print(f"Plot saved to {out_path}")
