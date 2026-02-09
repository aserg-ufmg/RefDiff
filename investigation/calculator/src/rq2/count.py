import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
import csv
import datetime

from src.lib.file_util import ensure_parent_dir
from src.lib.load_csv import load_csv_files

OUT_PATH_TEMPLATE = './output/refactoring_count_{ts}.pdf'

if __name__ == '__main__':
    df_all = load_csv_files("../result")

    # Step 1: Calculate percentages and absolute counts
    all_counts = {}
    all_absolute_counts = {}
    allowed_refactoring_types = ['CHANGE_SIGNATURE', 'EXTRACT', 'EXTRACT_MOVE', 'INLINE', 'MOVE', 'RENAME', 'MOVE_RENAME']

    if not df_all.empty and 'Lang' in df_all.columns:
        for lang, df in df_all.groupby('Lang'):
            df = df.copy()
            if 'RefactoringType' in df.columns:
                df['RefactoringType'] = df['RefactoringType'].replace(['INTERNAL_MOVE'], 'MOVE')
                df['RefactoringType'] = df['RefactoringType'].replace(['INTERNAL_MOVE_RENAME'], 'MOVE_RENAME')

                # Filter the DataFrame to include only allowed refactoring types
                filtered_df = df[df['RefactoringType'].isin(allowed_refactoring_types)]

                # Calculate normalized counts for the filtered DataFrame
                counts = filtered_df['RefactoringType'].value_counts(normalize=True) * 100
                all_counts[lang] = counts

                # Calculate absolute counts
                absolute_counts = filtered_df['RefactoringType'].value_counts()
                all_absolute_counts[lang] = absolute_counts


    # Prepare data for plotting
    # Use absolute counts (not percentages) for plotting
    plot_data = pd.DataFrame(all_absolute_counts).fillna(0)
    absolute_counts_data = pd.DataFrame(all_absolute_counts).fillna(0)

    # Check if there is data to plot
    if plot_data.empty:
        print("No data available to plot.")
        exit(0)
    # Transpose for plotting (languages on x-axis)
    ax = plot_data.T.plot(kind='bar', stacked=True, figsize=(10, 7))
    # To ensure the order of counts matches the plot, we align the absolute counts dataframe
    # with the percentage dataframe, which dictates the plot structure.
    aligned_absolute_counts = absolute_counts_data.reindex(index=plot_data.index, columns=plot_data.columns).fillna(0).astype(int)
    # Add counts on the bars
    for container in ax.containers:
        # The label for each container is the refactoring type
        refactoring_type = container.get_label()
        
        # Get the counts for this type across all languages from the aligned absolute counts
        if refactoring_type in aligned_absolute_counts.index:
            labels = aligned_absolute_counts.loc[refactoring_type].values
            
            # Create labels only for non-zero bars to avoid clutter
            display_labels = [f'{v}' if v > 0 else '' for v in labels]
            
            ax.bar_label(container, labels=display_labels, label_type='center', color='white', weight='bold')

    plt.title('Count of Refactoring Types by Language')
    plt.xlabel('Language')
    plt.ylabel('Count')
    plt.xticks(rotation=0)
    plt.legend(title='Refactoring Type', bbox_to_anchor=(1.05, 1), loc='upper left')
    plt.tight_layout()

    # Save the plot (include month-day and hour-minute, no year)
    out_path = OUT_PATH_TEMPLATE.format(ts=datetime.datetime.now().strftime("%m-%d_%H-%M"))
    ensure_parent_dir(out_path)
    plt.savefig(out_path)

    print(f"Plot saved to {out_path}")
