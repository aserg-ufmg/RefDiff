#!/bin/bash

set -euo pipefail

DEFAULT_ITEMS=(
  "c|obs-studio"
  "c|libuv"
  "c|tmux"
  "c|git"
	"c|linux"
	"c|netdata"
	"c|redis"
	"c|ijkplayer"
	"c|php-src"
	"c|wrk"
	"c|the_silver_searcher"
	"c|emscripten"
	"c|vim"
	"c|jq"
	"c|FFmpeg"
	"c|nuklear"
	"c|swoole-src"
	"c|curl"
	"c|toxcore"
	"c|darknet"

  "javascript|atom"
  "javascript|axios"
  "javascript|reveal.js"
  "javascript|express"
	"javascript|react"
	"javascript|vue"
	"javascript|d3"
	"javascript|react-native"
	"javascript|angular.js"
	"javascript|create-react-app"
	"javascript|jquery"
	"javascript|three.js"
	"javascript|socket.io"
	"javascript|redux"
	"javascript|webpack"
	"javascript|Semantic-UI"
	"javascript|meteor"
	"javascript|material-ui"
	"javascript|Chart.js"
)

# Determine items to run
if [ "$#" -gt 0 ]; then
	ITEMS=("$@")
else
	ITEMS=("${DEFAULT_ITEMS[@]}")
fi

# Concurrency control: max parallel jobs
MAX_PARALLEL=10
PIDS=()

run_and_log() {
	local lang="$1"
	local mode="$2"
	local repo="$3"
	local logdir="./script/logs"
	mkdir -p "$logdir"
	local logfile="$logdir/${repo}-${mode}.txt"
	./build/install/evaluation/bin/evaluation "$lang" "$mode" "$repo" > "$logfile" 2>&1 &
	local pid=$!
	echo "Started ${repo} (${lang} ${mode}) PID: ${pid} -> ${logfile}"
	PIDS+=("$pid")
}

# Wait until at least one slot is free (prune finished PIDs)
wait_for_slot() {
	while [ ${#PIDS[@]} -ge $MAX_PARALLEL ]; do
		removed=0
		for pid in "${PIDS[@]}"; do
			if ! kill -0 "$pid" 2>/dev/null; then
				wait "$pid" 2>/dev/null || true
				# remove pid from PIDS
				newpids=()
				for p in "${PIDS[@]}"; do
					if [ "$p" != "$pid" ]; then
						newpids+=("$p")
					fi
				done
				PIDS=("${newpids[@]}")
				removed=1
				break
			fi
		done
		if [ $removed -eq 0 ]; then
			sleep 0.5
		fi
	done
}

# Launch entries
for entry in "${ITEMS[@]}"; do
	lang="${entry%%|*}"
	repo="${entry#*|}"
	if [ -z "$lang" ] || [ -z "$repo" ] || [ "$lang" = "$repo" ]; then
		echo "Skipping malformed entry: $entry"
		continue
	fi

	run_and_log "$lang" head "$repo"
	wait_for_slot
done

# Wait for remaining jobs
for p in "${PIDS[@]}"; do
	if kill -0 "$p" >/dev/null 2>&1; then
		wait "$p"
	fi
done

echo "All jobs completed."
