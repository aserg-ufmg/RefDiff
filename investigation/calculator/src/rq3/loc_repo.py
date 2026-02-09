import pandas as pd
import glob
import os
import csv
import matplotlib.pyplot as plt
import datetime
import numpy as np

from src.lib.load_csv import load_csv_files
from src.lib.file_util import ensure_parent_dir

OUT_BEFORE_PATH = './output/extract_before_loc_repo_{ts}.pdf'
OUT_AFTER_PATH = './output/extract_after_loc_repo_{ts}.pdf'

if __name__ == '__main__':
    df_all = load_csv_files("../result")

    extract_data_list = []

    if not df_all.empty and 'Lang' in df_all.columns:
        for lang, df_lang in df_all.groupby('Lang'):
            if 'RefactoringType' in df_lang.columns and 'BeforeLOC' in df_lang.columns and 'AfterLOC' in df_lang.columns:
                extract_df = df_lang[df_lang['RefactoringType'] == 'EXTRACT'].copy()
                extract_data_list.append(extract_df)

    if extract_data_list:
        combined_extract_df = pd.concat(extract_data_list, ignore_index=True)

        combined_extract_df['BeforeLOC'] = pd.to_numeric(combined_extract_df['BeforeLOC'], errors='coerce')
        combined_extract_df['AfterLOC'] = pd.to_numeric(combined_extract_df['AfterLOC'], errors='coerce')

        combined_extract_df.dropna(subset=['BeforeLOC', 'AfterLOC'], inplace=True)

        # Clip BeforeLOC to a maximum.
        # combined_extract_df['BeforeLOC'] = combined_extract_df['BeforeLOC'].clip(upper=200)

        combined_extract_df['BeforeLOC'] = combined_extract_df['BeforeLOC'].astype(int)
        combined_extract_df['AfterLOC'] = combined_extract_df['AfterLOC'].astype(int)

        if not combined_extract_df.empty:
            plt.style.use('ggplot')

            # Order repos by language then repo name
            repo_lang = combined_extract_df[['Repo', 'Lang']].drop_duplicates().set_index('Repo')['Lang'].to_dict()
            lang_order = ['java', 'c', 'javascript', 'python', 'go', 'php', 'ruby']
            lang_rank = {lang: i for i, lang in enumerate(lang_order)}
            ordered_repos = sorted(repo_lang.keys(), key=lambda r: (lang_rank.get(repo_lang.get(r, ''), 999), r))

            # BeforeLOC boxplot by repo
            fig1, ax1 = plt.subplots(figsize=(28, 10))
            combined_extract_df.boxplot(column='BeforeLOC', by='Repo', ax=ax1, showfliers=False, showmeans=True,
                                        positions=range(len(ordered_repos)))
            ax1.set_title('LOC for EXTRACT Refactoring by Project (Before)')
            ax1.set_xlabel('Project (language in parenthesis)')
            ax1.set_ylabel('Lines of Code')
            labels = [f"{repo}\n({repo_lang.get(repo,'')})" for repo in ordered_repos]
            ax1.set_xticks(range(len(ordered_repos)))
            ax1.set_xticklabels(labels, rotation=60, ha='center')
            plt.suptitle('')
            plt.tight_layout()
            out_before_path = OUT_BEFORE_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
            ensure_parent_dir(out_before_path)
            plt.savefig(out_before_path, bbox_inches='tight')
            print(f"Saved BeforeLOC boxplot to {out_before_path}")
            plt.close(fig1)

            # AfterLOC boxplot by repo
            fig2, ax2 = plt.subplots(figsize=(28, 10))
            combined_extract_df.boxplot(column='AfterLOC', by='Repo', ax=ax2, showfliers=False, showmeans=True,
                                        positions=range(len(ordered_repos)))
            ax2.set_title('LOC for EXTRACT Refactoring by Project (After)')
            ax2.set_xlabel('Project (language in parenthesis)')
            ax2.set_ylabel('Lines of Code')
            ax2.set_xticks(range(len(ordered_repos)))
            ax2.set_xticklabels(labels, rotation=60, ha='center')
            plt.suptitle('')
            plt.tight_layout()
            out_after_path = OUT_AFTER_PATH.format(ts=datetime.datetime.now().strftime("%m%d_%H-%M"))
            ensure_parent_dir(out_after_path)
            plt.savefig(out_after_path, bbox_inches='tight')
            print(f"Saved AfterLOC boxplot to {out_after_path}")
            plt.close(fig2)
        else:
            print("No valid data to plot after cleaning.")
    else:
        print("No 'EXTRACT' refactoring data found to plot.")
