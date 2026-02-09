import os

def ensure_parent_dir(filepath):
    dirpath = os.path.dirname(filepath)
    if dirpath and not os.path.exists(dirpath):
        try:
            os.makedirs(dirpath, exist_ok=True)
        except Exception as e:
            print(f"Could not create directory {dirpath}: {e}")
