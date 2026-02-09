import pandas as pd
import matplotlib.pyplot as plt
import glob
import os
import datetime
import re

from src.lib.load_csv import load_csv_files
from src.lib.file_util import ensure_parent_dir

OUT_PATH_TEMPLATE = "./output/refactoring_distribution_{ts}.pdf"

ENTITY_NAMES_BY_LANG = {
    "java": ["Method"],
    "c": ["Function"],
    "javascript": ["Function"],
    "python": ["Function"],
    "go": ["function_declaration", "method_declaration"],
    "php": ["function_definition", "method_declaration"],
    "ruby": ["Method"],
}

if __name__ == "__main__":
    df = load_csv_files("../result")

    # Step 1: Calculate percentages and absolute counts
    all_counts = {}
    all_absolute_counts = {}
    allowed_refactoring_types = [
        "CHANGE_SIGNATURE",
        "EXTRACT",
        "EXTRACT_MOVE",
        "INLINE",
        "MOVE",
        "RENAME",
        "MOVE_RENAME",
    ]

    for lang, df_lang in df.groupby("Lang"):
        df_lang = df_lang.copy()
        if "RefactoringType" in df_lang.columns:
            if "Before" in df_lang.columns:
                entity_names = ENTITY_NAMES_BY_LANG.get(lang, [])
                if entity_names:
                    pattern = "|".join(re.escape(name) for name in entity_names)
                    df_lang = df_lang[
                        df_lang["Before"].astype(str).str.contains(pattern, na=False)
                    ]
                else:
                    df_lang = df_lang.iloc[0:0]

            df_lang["RefactoringType"] = df_lang["RefactoringType"].replace(
                ["INTERNAL_MOVE"], "MOVE"
            )
            df_lang["RefactoringType"] = df_lang["RefactoringType"].replace(
                ["INTERNAL_MOVE_RENAME"], "MOVE_RENAME"
            )

            # Filter the DataFrame to include only allowed refactoring types
            filtered_df = df_lang[
                df_lang["RefactoringType"].isin(allowed_refactoring_types)
            ]

            # Calculate normalized counts for the filtered DataFrame
            counts = filtered_df["RefactoringType"].value_counts(normalize=True) * 100
            all_counts[lang] = counts

            # Calculate absolute counts
            absolute_counts = filtered_df["RefactoringType"].value_counts()
            all_absolute_counts[lang] = absolute_counts

    # Prepare data for plotting
    plot_data = pd.DataFrame(all_counts).fillna(0)

    # Check if there is data to plot
    if plot_data.empty:
        print("No data available to plot.")
        exit(0)

    # Transpose for plotting (languages on x-axis)
    lang_order = ["java", "c", "javascript", "python", "go", "php", "ruby"]
    plot_data = plot_data.reindex(columns=lang_order)
    ax = plot_data.T.plot(kind="bar", stacked=True, figsize=(10, 7))

    # Add percentages on the bars
    for container in ax.containers:
        # The label for each container is the refactoring type
        refactoring_type = container.get_label()

        # Get the percentage values for this type across all languages
        if refactoring_type in plot_data.index:
            labels = plot_data.loc[refactoring_type].values

            # Create labels only for non-zero bars to avoid clutter
            display_labels = [f"{v:.1f}" if v > 1 else "" for v in labels]

            ax.bar_label(
                container,
                labels=display_labels,
                label_type="center",
                color="white",
                weight="bold",
                fontsize=8,
            )

    plt.title("Distribution of Refactoring Types by Language")
    plt.xlabel("Language")
    plt.ylabel("Percentage (%)")
    plt.xticks(rotation=0)
    plt.legend(title="Refactoring Type", bbox_to_anchor=(1.05, 1), loc="upper left")
    plt.tight_layout()

    # Save the plot (include month-day and hour-minute, no year)
    out_path = OUT_PATH_TEMPLATE.format(
        ts=datetime.datetime.now().strftime("%m-%d_%H-%M")
    )
    ensure_parent_dir(out_path)
    plt.savefig(out_path)

    print(f"Plot saved to {out_path}")
