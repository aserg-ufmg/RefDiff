#!/usr/bin/env bash
set -euo pipefail

# Run language validators in parallel and collect logs and exit codes.
# Usage: ./validate.sh

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BIN="$ROOT_DIR/build/install/plugin-generator/bin/plugin-generator"

if [ ! -x "$BIN" ]; then
  echo "Error: plugin-generator binary not found or not executable: $BIN" >&2
  echo "Build the project first (e.g. ./gradlew :plugin-generator:installDist)" >&2
  exit 2
fi

mkdir -p "$ROOT_DIR/logs"

# langs=(java c javascript python go php ruby)
langs=(javascript)
# langs=(php)
# langs=(ruby)
pids=()
logs=()

trap 'echo "Killing children..."; kill "${pids[@]:-}" 2>/dev/null || true' EXIT

for l in "${langs[@]}"; do
  out="$ROOT_DIR/logs/validator-${l}.log"
  echo "Starting validator for: $l -> $out"
  "$BIN" "$l" >"$out" 2>&1 &
  pid=$!
  pids+=("$pid")
  logs+=("$out")
done

failed=0
for i in "${!pids[@]}"; do
  pid=${pids[$i]}
  lang=${langs[$i]}
  out=${logs[$i]}
  if wait "$pid"; then
    echo "[$lang] finished: $out"
  else
    echo "[$lang] FAILED (see $out)"
    failed=1
  fi
done

if [ "$failed" -ne 0 ]; then
  echo "One or more validators failed. See logs in: $ROOT_DIR/logs" >&2
  exit 1
fi

echo "All validators completed successfully. Logs:"
for f in "${logs[@]}"; do
  echo " - $f"
done

exit 0
