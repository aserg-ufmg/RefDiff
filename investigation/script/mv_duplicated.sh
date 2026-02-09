#!/usr/bin/env sh
set -eu

usage() {
  echo "Usage: $0 <directory>" 1>&2
  echo "Move files named like {name}-{date}-{time}-{num}.csv where files with the same `name` and `num` are treated as duplicates into <directory>/duplicated/" 1>&2
  exit 2
}

DIR=${1:-}
[ -n "$DIR" ] || usage
[ -d "$DIR" ] || { echo "Not a directory: $DIR" 1>&2; exit 1; }

DUP_ROOT="$DIR/duplicated"
mkdir -p "$DUP_ROOT"

TMP=$(mktemp)
trap 'rm -f "$TMP"' EXIT

# Collect name, num and file path triplets. Filename pattern assumed: {name}-{date}-{time}-{num}.csv
for f in "$DIR"/*; do
  [ -f "$f" ] || continue
  fname=$(basename "$f")
  # skip directories and non-csv
  case "$fname" in
    *.csv) ;;
    *) continue ;;
  esac
  base=${fname%.*}
  # extract last hyphen-separated token as num
  num=${base##*-}
  # if num equals base (no hyphen present), skip
  if [ "$num" = "$base" ]; then
    continue
  fi
  # derive `name` by removing the last three hyphen-separated tokens (num, time, date)
  tmp1=${base%-*}   # remove num
  tmp2=${tmp1%-*}  # remove time
  name_part=${tmp2%-*}  # remove date; if no more hyphen, yields tmp2
  if [ -z "$name_part" ]; then
    name_part="$tmp2"
  fi
  printf '%s\t%s\t%s\n' "$name_part" "$num" "$f" >> "$TMP"
done

# For each (name,num) key that appears more than once, move one file to duplicated/
awk -F'\t' '{key=$1"\t"$2; cnt[key]++; files[key]=files[key] RS $3} END {for (k in cnt) if (cnt[k]>1) print k}' "$TMP" | while IFS= read -r key; do
  [ -n "$key" ] || continue
  # split key into name and num
  name=$(printf '%s' "$key" | awk -F'\t' '{print $1}')
  num=$(printf '%s' "$key" | awk -F'\t' '{print $2}')
  target_dir="$DUP_ROOT"
  # Show duplicated group (all files sharing this name+num)
  files=$(awk -F'\t' -v n="$name" -v m="$num" '$1==n && $2==m {print $3}' "$TMP")
  if [ -n "$files" ]; then
    echo "Duplicate: name='$name', num='$num'"
    echo "$files" | sed 's/^/  /'
  fi

  # Move only one file from the duplicated group (the first one found)
  src=$(awk -F'\t' -v n="$name" -v m="$num" '$1==n && $2==m {print $3; exit}' "$TMP")
  if [ -n "$src" ] && [ -f "$src" ]; then
    base=$(basename "$src")
    dest="$target_dir/$base"
    if [ -e "$dest" ]; then
      i=1
      while [ -e "$target_dir/${base}.$i" ]; do i=$((i+1)); done
      dest="$target_dir/${base}.$i"
    fi
    mv "$src" "$dest"
    echo "Moved: $base -> $DUP_ROOT/"
  fi
done

echo "Moved files with duplicated name+num into: $DUP_ROOT"
