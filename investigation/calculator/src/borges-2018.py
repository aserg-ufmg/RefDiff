#!/usr/bin/env python3
import csv
import json
import os
import time
import urllib.request
import urllib.error
from collections import defaultdict
from dotenv import load_dotenv

CSV_PATH = './borges-repos-2018.csv'
OUT_PATH = './borges-2018.json'
ALLOWED_LANGUAGES = {'Java', 'C', 'JavaScript', 'Go', 'Python', 'PHP', 'Ruby'}
TOP_N = 5


def get_primary_language(full_name, token=None, max_retries=5, pause=0.5):
    """Return the repository's primary language using the GitHub REST API.

    Args:
        full_name (str): repository full name in the form 'owner/repo'.
        token (str|None): optional GitHub token. If None, reads from
            environment variable GITHUB_TOKEN.

    Returns:
        str|None: primary language (e.g. 'Java') or None if not found.

    Raises:
        ValueError: if `full_name` is not in the expected form.
        urllib.error.HTTPError: for non-404 HTTP errors.
    """
    if '/' not in full_name:
        raise ValueError("full_name must be 'owner/repo'")
    if token is None:
        token = os.environ.get('GITHUB_TOKEN')
    owner, repo = full_name.split('/', 1)
    url = f'https://api.github.com/repos/{owner}/{repo}'

    last_exc = None
    for attempt in range(max_retries):
        req = urllib.request.Request(url)
        req.add_header('Accept', 'application/vnd.github.v3+json')
        if token:
            req.add_header('Authorization', f'token {token}')
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.load(resp)
                lang = data.get('language')
                if pause and pause > 0:
                    time.sleep(pause)
                return lang
        except urllib.error.HTTPError as e:
            last_exc = e
            if e.code == 404:
                return None
            if e.code == 403:
                reset = None
                try:
                    if hasattr(e, 'headers') and e.headers is not None:
                        reset = e.headers.get('X-RateLimit-Reset')
                        if reset is not None:
                            reset = int(reset)
                except Exception:
                    reset = None
                if reset:
                    wait = max(reset - int(time.time()), 1) + 5
                else:
                    wait = (2 ** attempt) * 5
                time.sleep(wait)
                continue
            raise
        except urllib.error.URLError as e:
            last_exc = e
            wait = (2 ** attempt)
            time.sleep(wait)
            continue
    if last_exc:
        return None
    return None

def top_by_lang_domain():
    groups = defaultdict(lambda: defaultdict(list))
    with open(CSV_PATH, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f, delimiter=';')
        for row in reader:
            lang = (row.get('language') or '').strip()
            if not lang or lang not in ALLOWED_LANGUAGES:
                continue
            domain = (row.get('domain') or '').strip() or 'Unknown'
            try:
                stars = int((row.get('stargazers_count') or '0').replace('"','').strip())
            except Exception:
                stars = 0
            full = row.get('full_name').strip()
            lower_full = full.lower().rstrip('/')
            url = f'https://github.com/{lower_full}.git' if lower_full else ''
            groups[lang][domain].append({'name': full, 'stars': stars, 'url': url})

        out = {}
        for lang in sorted(groups.keys()):
            out[lang] = {}
            for domain in sorted(groups[lang].keys()):
                items = sorted(groups[lang][domain], key=lambda x: x['stars'], reverse=True)[:TOP_N]
                primary_language_filtered = []
                for it in items:
                    print("Checking primary language for:", it['name'])
                    primary = get_primary_language(it['name'])
                    if primary is None:
                        continue
                    if primary.lower() == lang.lower():
                        primary_language_filtered.append(it)
                out[lang][domain] = [{'name': it['name'], 'stars': it['stars'], 'url': it.get('url', '')} for it in primary_language_filtered]                
        try:
            with open(OUT_PATH, 'w', encoding='utf-8') as of:
                json.dump(out, of, ensure_ascii=False, indent=2)
        except Exception:
            print(json.dumps(out, ensure_ascii=False, indent=2))
        return

if __name__ == '__main__':
    load_dotenv()
    top_by_lang_domain()

