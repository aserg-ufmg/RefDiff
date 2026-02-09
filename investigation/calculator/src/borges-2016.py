import pandas as pd
import os
import json

TOP_N = 5

def load_borges_data():
    """
    Loads data from ./borges-repos-2016.csv.
    This function is inspired by load_csv_files in main.py.
    """
    csv_path = './borges-repos-2016.csv'
    
    try:
        df = pd.read_csv(csv_path)
        return df
    except FileNotFoundError:
        print(f"Error: The file at {csv_path} was not found.")
        print(f"Please ensure the script is run from the 'investigation/calculator/src' directory or that the path is correct.")
        return None
    except Exception as e:
        print(f"An error occurred: {e}")
        return None

if __name__ == '__main__':
    borges_data = load_borges_data()
    if borges_data is not None:
        target_languages = ['java', 'c', 'javascript', 'typescript', 'php', 'go', 'python', 'ruby']

        borges_data['Stars'] = pd.to_numeric(borges_data['Stars'].astype(str).str.replace(',', ''), errors='coerce')
        borges_data.dropna(subset=['Stars'], inplace=True)
        borges_data['Stars'] = borges_data['Stars'].astype(int)

        borges_data['Language'] = borges_data['Language'].str.lower()
        borges_data.dropna(subset=['Language', 'Domain'], inplace=True)

        filtered_df = borges_data[borges_data['Language'].isin(target_languages)]

        out = {}
        for lang in sorted(target_languages):
            lang_df = filtered_df[filtered_df['Language'] == lang]
            if lang_df.empty:
                out[lang] = {}
                continue

            domains = sorted(lang_df['Domain'].unique())
            out[lang] = {}
            for domain in domains:
                domain_df = lang_df[lang_df['Domain'] == domain]
                top_n = domain_df.sort_values(by='Stars', ascending=False).head(TOP_N)
                repos = []
                for _, row in top_n.iterrows():
                    repos.append({'name': row.get('Name') if 'Name' in row else None,
                                  'stars': int(row['Stars']) if not pd.isna(row['Stars']) else 0,
                                  'url': row.get('URL') if 'URL' in row else None})
                out[lang][domain] = repos

        print(json.dumps(out, ensure_ascii=False, indent=2))
