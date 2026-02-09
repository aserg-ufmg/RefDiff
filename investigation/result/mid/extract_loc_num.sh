#!/usr/bin/env bash
set -euo pipefail

print_usage() {
  cat <<'USAGE'
Usage: extract_loc_num.sh [options] [file...]

Read lines from files (or stdin when no files are given), find occurrences of
LOC:{<number>} and either print the original line (BEFORE) + extracted number(s)
or, with --pairs, print a compact comma-separated list of extracted numbers per
matching input line.

  Options:
  -p, --pairs      Print only the numbers joined by commas (one match-per-line)
  -n, --numbers    Alias for --pairs (print numbers only)
  -e, --extract-only  Only consider lines starting with EXTRACT or EXTRACT_MOVE
  -b, --before-only   Print only the first LOC number (the "before" value) per line
  -a, --after-only    Print only the second LOC number (the "after" value) per line
  -h, --help       Show this help

Examples:
  echo 'Example LOC:{123} foo' | ./extract_loc_num.sh
  echo 'Example LOC:{123} foo' | ./extract_loc_num.sh --pairs
  ./extract_loc_num.sh file.txt
USAGE
}

process_stream() {
  # Read line-by-line, preserve trailing lines without newline
  while IFS= read -r line || [ -n "$line" ]; do
    # If extract-only mode is enabled, skip lines that don't start with EXTRACT or EXTRACT_MOVE
    if [ "${EXTRACT_ONLY:-0}" = "1" ]; then
      if ! printf '%s' "$line" | grep -qE '^(EXTRACT|EXTRACT_MOVE)\b'; then
        continue
      fi
    fi
    # find matches like LOC:{123} or LOC:123
    matches=$(printf '%s\n' "$line" | grep -oE 'LOC:\{?[0-9]+\}?' || true)
    if [ -n "$matches" ]; then
      # convert LOC:{123} -> 123 and join multiple matches with commas
      nums=$(printf '%s\n' "$matches" | sed -E 's/LOC:\{?([0-9]+)\}?/\1/' | paste -sd, - | sed 's/,/, /g')
      if [ "${AFTER_ONLY:-0}" = "1" ]; then
        # print only the second number (after). If missing, print empty line.
        second=$(printf '%s' "$nums" | awk -F", *" '{ if (NF>=2) print $2; else print "" }')
        printf '%s\n' "$second"
      elif [ "${BEFORE_ONLY:-0}" = "1" ]; then
        # print only the first number (before)
        first=$(printf '%s' "$nums" | sed -E 's/,.*//')
        printf '%s\n' "$first"
      elif [ "${PRINT_MODE:-default}" = "pairs" ]; then
        printf '%s\n' "$nums"
      else
        printf 'BEFORE: %s\nAFTER:  %s\n---\n' "$line" "$nums"
      fi
    fi
  done
}

# parse options (only --pairs/-p and --help/-h are supported)
PRINT_MODE=default
while [ "$#" -gt 0 ]; do
  case "$1" in
    -p|--pairs)
      PRINT_MODE=pairs
      shift
      ;;
    -n|--numbers)
      PRINT_MODE=pairs
      shift
      ;;
    -e|--extract-only)
      EXTRACT_ONLY=1
      shift
      ;;
    -b|--before-only)
      BEFORE_ONLY=1
      shift
      ;;
    -a|--after-only)
      AFTER_ONLY=1
      shift
      ;;
    -h|--help)
      print_usage
      exit 0
      ;;
    --) shift; break ;;
    -*)
      printf 'Unknown option: %s\n' "$1" >&2
      print_usage
      exit 2
      ;;
    *) break ;;
  esac
done

if [ "$#" -eq 0 ]; then
  process_stream
else
  for f in "$@"; do
    if [ ! -r "$f" ]; then
      printf 'Warning: cannot read %s\n' "$f" >&2
      continue
    fi
    process_stream < "$f"
  done
fi
