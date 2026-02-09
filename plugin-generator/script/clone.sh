
#!/usr/bin/env bash
set -euo pipefail

usage() {
	cat <<EOF
Usage: $0 [-f file] [-b branch] [-o outdir] <repo-url>
	-f FILE   file with one repo URL per line (ignores other args except -b,-o)
	-b BRANCH branch/tag to checkout (optional). If omitted, detects remote default (main/master).
	-o DIR    parent output directory (default: ./repo)
	repo-url  single repository URL to clone

Note: This script always performs a shallow clone with depth=1 (snapshot). The depth option
has been removed and is fixed to 1 to fetch only the latest snapshot.
Examples:
	$0 https://github.com/user/repo.git
	$0 -b main -o repos https://github.com/user/repo.git
	$0 -f repos.txt -o repos
EOF
}

FILE=""
# depth is fixed to 1 (shallow snapshot)
DEPTH="1"
OUTDIR="./repo"


parse_args() {
	while getopts ":f:o:h" opt; do
		case $opt in
			f) FILE="$OPTARG" ;;
			o) OUTDIR="$OPTARG" ;;
			h) usage; exit 0 ;;
			\?) echo "Invalid option -$OPTARG" >&2; usage; exit 1 ;;
		esac
	done
	shift $((OPTIND-1))
}

ensure_outdir() {
	mkdir -p "$OUTDIR"
}

clone_repo() {
	local url="$1"
	local target="$2"
	# If target already exists, skip cloning.
	if [ -d "$target" ]; then
		if [ -d "$target/.git" ]; then
			echo "Skipping already-cloned repo: $target"
			return 0
		else
			echo "Skipping existing target (not a git repo): $target" >&2
			return 0
		fi
	fi
	# Detect remote default branch (e.g., main or master) and use it.
	default_branch=$(git ls-remote --symref "$url" HEAD 2>/dev/null | awk '/^ref:/ {print $2}' | sed 's#refs/heads/##' | head -n1 || true)
	if [ -n "$default_branch" ]; then
		echo "Cloning $url -> $target (depth=$DEPTH, branch=$default_branch)"
		git clone --depth "$DEPTH" --branch "$default_branch" "$url" "$target"
	else
		echo "Cloning $url -> $target (depth=$DEPTH) (no remote default branch detected)"
		git clone --depth "$DEPTH" "$url" "$target"
	fi
}

is_http_url() {
	case "$1" in
		http://*|https://*) return 0 ;;
		*) return 1 ;;
	esac
}

process_file() {
	local file="$1"
	if [ ! -f "$file" ]; then
		echo "File not found: $file" >&2
		exit 1
	fi
	while IFS= read -r line || [ -n "$line" ]; do
		line="$(echo "$line" | sed -e 's/^[[:space:]]*//;s/[[:space:]]*$//')"
		[ -z "$line" ] && continue
		if ! is_http_url "$line"; then
			echo "Skipping non-http repository URL: $line" >&2
			continue
		fi
		name=$(basename "${line%.git}")
		target="$OUTDIR/$name"
		clone_repo "$line" "$target"
	done < "$file"
}

clone_single_url() {
	local url="$1"
	if ! is_http_url "$url"; then
		echo "Skipping non-http repository URL: $url" >&2
		exit 1
	fi
	name=$(basename "${url%.git}")
	target="$OUTDIR/$name"
	clone_repo "$url" "$target"
}

main() {
	parse_args "$@"
	ensure_outdir
	if [ -n "${FILE}" ]; then
		process_file "$FILE"
	else
		if [ $# -eq 0 ]; then
			usage
			exit 1
		fi
		url="$1"
		clone_single_url "$url"
	fi
}

main "$@"

exit 0

