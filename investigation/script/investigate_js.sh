#!/bin/bash

set -e

APP_BIN="./build/install/investigation/bin/investigation"

# for js
ITEMS=(
  "javascript|https://github.com/adobe/brackets.git" # App
  "javascript|https://github.com/moment/moment.git" # Non-web
  "javascript|https://github.com/gulpjs/gulp.git" # Software tool
  "javascript|https://github.com/nodejs/node.git" # System
  "javascript|https://github.com/facebook/react.git" # Web

  "javascript|https://github.com/resume/resume.github.com.git" # Application software (2nd)
  "javascript|https://github.com/lodash/lodash.git"      # Non-web libraries and frameworks (2nd)
  "javascript|https://github.com/yarnpkg/yarn.git"       # Software tools (2nd)
  "javascript|https://github.com/typicode/json-server.git" # System software (2nd)
  "javascript|https://github.com/angular/angular.js.git" # Web libraries and frameworks (2nd)
)

# Ensure logs directory exists
mkdir -p logs

# Maximum concurrent processes (adjustable)
MAX_CONCURRENT=15

# Active background PIDs
PIDS=()

for entry in "${ITEMS[@]}"; do
  # parse entry "lang|repo_url"
  lang="${entry%%|*}"
  repo_url="${entry#*|}"

  if [ -z "$lang" ] || [ -z "$repo_url" ] || [ "$lang" = "$repo_url" ]; then
    echo "Skipping malformed entry: $entry"
    continue
  fi

  repo_name=$(basename "$repo_url" .git)
  safe_repo_name=$(echo "$repo_name" | sed 's/[^a-zA-Z0-9_.-]/_/g')
  log_file="logs/js_with_change_signature/out_${lang}_${safe_repo_name}.log"

  # Launch one process per repository, passing --repo to the application
  # "$APP_BIN" --language "$lang" --repo "$repo_url" --resume > "$log_file" 2>&1 &
  "$APP_BIN" --language "$lang" --repo "$repo_url" --result-dir result/javascript/with_change_signature  > "$log_file" 2>&1 &
  pid=$!
  PIDS+=("$pid")
  echo "Started process for $lang repo $repo_url (PID: $pid) -> $log_file"

  # If we've reached the concurrency limit, wait until at least one PID finishes.
  while :; do
    # Prune finished PIDs from PIDS
    active=()
    for p in "${PIDS[@]}"; do
      if kill -0 "$p" >/dev/null 2>&1; then
        active+=("$p")
      fi
    done
    PIDS=("${active[@]}")

    if [ "${#PIDS[@]}" -lt "$MAX_CONCURRENT" ]; then
      break
    fi

    sleep 10
  done
done

# After starting all entries, wait for any remaining background processes
for p in "${PIDS[@]}"; do
  if kill -0 "$p" >/dev/null 2>&1; then
    wait "$p"
  fi
done

echo "All scripts have completed."