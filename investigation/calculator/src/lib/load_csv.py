import pandas as pd
import glob
import os
import re

"""
read all csv files from base_path/<language>/*.csv
return a DataFrame containing all records with additional columns 'Lang' and 'Repo'
"""
def load_csv_files(base_path):
    languages = ["java", "c", "javascript", "python", "go", "php", "ruby"]
    column_names = [
        "Commit",
        "RefactoringType",
        "Before",
        "After",
        "BeforeLOC",
        "AfterLOC",
        "Lang",
        "Repo",
    ]

    # Files to exclude by language (auto-generated/build artifacts)
    exclude_names_by_lang = {
        "java": [
            "r.java", "buildconfig.java", "manifest.java", "pom.xml", "build.gradle",
            "settings.gradle", "gradle.properties", "gradlew", "gradlew.bat"
        ],
        "c": [
            "configure", "config.h", "config.h.in", "config.log", "config.status",
            "makefile", "cmakecache.txt", "cmakefiles.txt", "cmake_install.cmake",
            "compile_commands.json"
        ],
        "javascript": [
            "package-lock.json", "yarn.lock", "pnpm-lock.yaml", "npm-shrinkwrap.json",
            "package.json", "rollup.config.js", "webpack.config.js", "vite.config.js",
            "parcel.config.js", "tsconfig.json", "babel.config.js", ".babelrc", "lint-md.js",
            "lodash.js", "lodash.src.js", # bundled library files
        ],
        "python": [
            "setup.py", "setup.cfg", "pyproject.toml", "requirements.txt", "pipfile",
            "pipfile.lock", "poetry.lock", "pdm.lock", "generated.py"
        ],
        "go": [
            "go.mod", "go.sum", "vendor/modules.txt", "bindata.go", "zz_generated.go",
            "zz_generated.deepcopy.go", "mock_gen.go", "mockgen.go", "wire_gen.go",
            "rewrite", "pb.go", "gen.go", "generated.go",
        ],
        "php": [
            "composer.json", "composer.lock", "autoload.php", "autoload_real.php",
            "autoload_static.php", "autoload_psr4.php", "autoload_classmap.php"
        ],
        "ruby": [
            "gemfile", "gemfile.lock", "rakefile", "gemspec", "version.rb",
            "schema.rb", "routes.rb"
        ],
    }
    exclude_dirs_by_lang = {
        "java": [
            "target", "build", "out", ".gradle", ".mvn", "generated", "gen",
            "build/generated", "build/resources", "build/tmp"
        ],
        "c": [
            "build", "cmake-build-debug", "cmake-build-release", "cmake-build-relwithdebinfo",
            "cmake-build-minsizerel", "autom4te.cache"
        ],
        "javascript": [
            "node_modules", "dist", "build", "out", ".next", ".nuxt", ".cache",
            "coverage", "vendor", "min"
        ],
        "python": [
            "__pycache__", "build", "dist", ".eggs", ".pytest_cache", ".mypy_cache",
            ".tox", ".venv", "venv", "site-packages"
        ],
        "go": [
            "vendor", "bin", "pkg", "dist", "kubernetes/typed",
        ],
        "php": [
            "vendor", "cache", "storage", "build"
        ],
        "ruby": [
            "vendor", "bundle", ".bundle", "log", "tmp", "coverage"
        ],
    }

    all_records = []
    for lang in languages:
        path = os.path.join(base_path, lang)
        csv_files = glob.glob(os.path.join(path, "*.csv"))
        if not csv_files:
            print(f"No CSV files found for {lang}")
            continue

        for file in csv_files:
            repo_name = os.path.splitext(os.path.basename(file))[0].split('-')[0]  # take part before '-'
            df_file = None
            # Try reading normally (let pandas infer header). If that fails, fallback to python engine and skip bad lines.
            try:
                df_file = pd.read_csv(file, dtype=str, encoding="utf-8")
            except Exception as e:
                print(f"Primary read failed for {file}: {e}. Trying python engine with relaxed parsing.")
                try:
                    df_file = pd.read_csv(file, dtype=str, encoding="utf-8", engine="python", on_bad_lines="skip")
                except Exception as e2:
                    print(f"Fallback read failed for {file}: {e2}. Skipping file.")
                    continue

            if df_file is None:
                continue

            df_file = df_file.fillna("")  # Fill NaN with empty string

            # If file had no header and exactly 6 columns, assign expected column names
            if df_file.shape[1] == 6:
                df_file.columns = ["Commit", "RefactoringType", "Before", "After", "BeforeLOC", "AfterLOC"]

            # Ensure required columns exist; add empty columns if missing
            for required in ["Commit", "RefactoringType", "Before", "After", "BeforeLOC", "AfterLOC"]:
                if required not in df_file.columns:
                    df_file[required] = ""

            df_file["Lang"] = lang
            df_file["Repo"] = repo_name

            # Filter out rows whose Before/After paths include excluded filenames
            excluded = set(name.lower() for name in exclude_names_by_lang.get(lang, []))
            excluded_dirs = set(name.lower().strip("/\\") for name in exclude_dirs_by_lang.get(lang, []))
            if excluded or excluded_dirs:
                before_paths = df_file["Before"].astype(str).str.lower().str.replace("\\", "/", regex=False)
                after_paths = df_file["After"].astype(str).str.lower().str.replace("\\", "/", regex=False)
                mask = True
                for name in excluded:
                    mask = mask & (~before_paths.str.contains(name, regex=False)) & (~after_paths.str.contains(name, regex=False))
                for dir_name in excluded_dirs:
                    mask = mask & (~before_paths.str.contains(dir_name, regex=False)) & (~after_paths.str.contains(dir_name, regex=False))
                df_file = df_file[mask]
            all_records.append(df_file)
    if not all_records:
        return pd.DataFrame(columns=column_names)
    return pd.concat(all_records, ignore_index=True)
