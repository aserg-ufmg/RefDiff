
#!/usr/bin/env bash
set -euo pipefail

# `DIR_EXT_MAP` maps a directory (prefix) to a comma-separated list of extensions.
# Format: "path:ext1,ext2". No leading dots on extensions.
# When `DIR_EXT_MAP` is non-empty the script will process only the mapped
# directories and use the listed extensions per-directory.
DIR_EXT_MAP=(
  "./repo/mall:java"
  "./repo/spring-boot:java"
  "./repo/elasticsearch:java"
  "./repo/ghidra:java"
  "./repo/spring-framework:java"
  "./repo/linux:c,h"
  "./repo/scrcpy:c,h"
  "./repo/netdata:c,h"
  "./repo/Ventoy:c,h"
  "./repo/redis:c,h"
  "./repo/react:js,jsx"
  "./repo/next.js:js,jsx"
  "./repo/node:js,jsx"
  "./repo/three.js:js,jsx"
  "./repo/axios:js,jsx"
  "./repo/ollama:go"
  "./repo/go:go"
  "./repo/kubernetes:go"
  "./repo/frp:go"
  "./repo/gin:go"
  "./repo/coolify:php"
  "./repo/framework:php"
  "./repo/server:php"
  "./repo/symfony:php"
  "./repo/jQuery-File-Upload:php"
  "./repo/AutoGPT:py"
  "./repo/stable-diffusion-webui:py"
  "./repo/transformers:py"
  "./repo/langflow:py"
  "./repo/youtube-dl:py"
  "./repo/rails:rb"
  "./repo/maybe:rb"
  "./repo/jekyll:rb"
  "./repo/mastodon:rb"
  "./repo/huginn:rb"
)

# ./repo/mall: 526 ←
# ./repo/spring-boot: 8143
# ./repo/elasticsearch: 26043
# ./repo/ghidra: 15312
# ./repo/spring-framework: 9150

# ./repo/linux: 62666
# ./repo/scrcpy: 173 ←
# ./repo/netdata: 1308
# ./repo/Ventoy: 723
# ./repo/redis: 739

# ./repo/react: 3867 ←
# ./repo/next.js: 9958
# ./repo/node: 19207
# ./repo/three.js: 1567
# ./repo/axios: 164 ←

# ./repo/ollama: 445 ←
# ./repo/go: 11014
# ./repo/kubernetes: 16551
# ./repo/frp: 244
# ./repo/gin: 96

# ./repo/coolify: 1456 ←
# ./repo/framework: 2752
# ./repo/server: 5312
# ./repo/symfony: 10071
# ./repo/jQuery-File-Upload: 2

# ./repo/AutoGPT: 769 ←
# ./repo/stable-diffusion-webui: 213
# ./repo/transformers: 3828
# ./repo/langflow: 1682
# ./repo/youtube-dl: 902

# ./repo/rails: 3381 ←
# ./repo/maybe: 794 ←
# ./repo/jekyll: 160
# ./repo/mastodon: 2989
# ./repo/huginn: 431

usage() {
	cat <<EOF
Usage: $0 [-e ext[,ext...]] [path]

Count files under [path] (defaults to current directory).

Options:
	-e EXT     Comma-separated extension(s) to count (e.g. -e py or -e "py,java").
						 Leading dot is optional. Can be specified multiple times.
	-h         Show this help and exit.

Additional configuration:
	Configure per-directory extension mappings by editing `DIR_EXT_MAP`.
	Use entries like "path:ext1,ext2". When `DIR_EXT_MAP` is non-empty the
	script will process only those mapped directories and use the listed
	extensions per-directory.

Examples:
	$0                # count all files
	$0 -e py         # count Python files
	$0 -e py,java src # count .py and .java files under src
EOF
}

if [ "$#" -eq 0 ]; then
	# no args: count all files in current dir
	:
fi

exts=()
while getopts ":e:h" opt; do
	case "$opt" in
		e)
			IFS=',' read -ra parts <<<"$OPTARG"
			for p in "${parts[@]}"; do
				# normalize: remove leading dot if present
				p="${p#.}"
				[ -n "$p" ] && exts+=("$p")
			done
			;;
		h)
			usage
			exit 0
			;;
		:) echo "Option -$OPTARG requires an argument." >&2; usage; exit 2 ;;
		*) echo "Invalid option: -$OPTARG" >&2; usage; exit 2 ;;
	esac
done
shift $((OPTIND-1))

# When DIR_EXT_MAP is non-empty we will use it as the sole source of
# directories and extensions. If it's empty, existing positional/-e
# behaviour applies (counting all files or using `-e`).

# collect positional args as paths; if none, use current directory
paths=()
if [ "$#" -gt 0 ]; then
	# remaining args are paths
	for p in "$@"; do
		paths+=("$p")
	done
else
	paths+=(".")
fi

# If DIR_EXT_MAP is set (non-empty), derive paths from its keys and
# ignore positional args / -e; otherwise keep the previously-collected paths.
if [ ${#DIR_EXT_MAP[@]} -gt 0 ]; then
	paths=()
	for entry in "${DIR_EXT_MAP[@]}"; do
		IFS=':' read -r mdir _ <<<"$entry"
		paths+=("$mdir")
	done
	# clear any global exts since per-dir mappings are authoritative
	exts=()
fi

# Helper: for a given path, return the extension list (space-separated)
# based on DIR_EXT_MAP. If no mapping matches, return the global `exts`.
get_exts_for_path() {
	local p="$1"
	local entry mdir mexts
	for entry in "${DIR_EXT_MAP[@]}"; do
		IFS=':' read -r mdir mexts <<<"$entry"
		# match exact dir or path under the mapping dir
		if [ "$p" = "$mdir" ] || [[ "$p" == "$mdir"/* ]]; then
			# split comma-separated mexts into space-separated list
			IFS=',' read -ra tmp <<<"$mexts"
			# normalize (remove leading dots)
			for i in "${!tmp[@]}"; do
				tmp[$i]="${tmp[$i]#.}"
			done
			echo "${tmp[@]}"
			return 0
		fi
	done
	# no mapping matched: return global exts (may be empty)
	if [ ${#exts[@]} -gt 0 ]; then
		echo "${exts[@]}"
	else
		echo ""
	fi
}

total=0
for path in "${paths[@]}"; do
	if [ ! -e "$path" ]; then
		echo "Path not found: $path" >&2
		continue
	fi

	# determine extensions for this path (may be from DIR_EXT_MAP or global exts)
	read -r -a exts_for_path <<< "$(get_exts_for_path "$path")"

	if [ ${#exts_for_path[@]} -eq 0 ]; then
		# no extensions specified for this path: count all regular files
		cnt=$(find "$path" -type f | wc -l)
	else
		# build predicate array for -iname patterns for this path
		pred=()
		for ext in "${exts_for_path[@]}"; do
			pred+=( -iname "*.${ext}" -o )
		done
		# remove trailing -o
		unset 'pred[${#pred[@]}-1]'

		# run find with the predicate
		cnt=$(find "$path" -type f \( "${pred[@]}" \) | wc -l)
	fi

	# trim whitespace from wc output
	cnt=$(printf "%s" "$cnt" | tr -d '[:space:]')
	echo "$path: $cnt"

	# add to total (ensure numeric)
	total=$((total + cnt))
done

echo "Total: $total"

exit 0

