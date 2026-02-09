# Premise
To run borges-2018.py, you need to set your GitHub token in the `.env` file.
Because this script accesses the GitHub API to get the repository language information.

```
python3 -m venv venv
venv/bin/pip install -r requirements.txt
```

```
venv/bin/python -m src.main
```

## Caution:
Calculate the result with all csv files in the `investigation/result/{language}/` directory.
If you ignore some files, you should move them out of the directory.


```bash
python3 -m pip install --user virtualenv
~/.local/bin/virtualenv venv

venv/bin/pip install -r requirements.txt
```