#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"

if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven was not found on PATH. Install Maven 3.9+ and JDK 21 first." >&2
    exit 1
fi

echo "Starting Wasl-e-Ta'am..."
exec mvn -B javafx:run
