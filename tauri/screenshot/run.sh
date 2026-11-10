#!/usr/bin/env bash
# sample/ を作業用の Workspace に複製して sidecar を起動し、Tauri の API を差し替えた画面をブラウザで操作する。
# Linux 用（Claude Code on the web で動かす想定）
#   run.sh        ヘルプの画面イメージ（public/help/images）を撮り直す（capture.cjs）
#   run.sh smoke  画面を一通り操作し、ブラウザのエラーが出ないかを確かめる（smoke.cjs）
set -euo pipefail

# 打ち間違いはビルド・起動の前に止める
mode="${1:-capture}"
case "$mode" in
capture | smoke) ;;
*)
	echo "使い方: run.sh [capture|smoke]" >&2
	exit 2
	;;
esac

here=$(cd "$(dirname "$0")" && pwd)
tauri=$(dirname "$here")
repo=$(dirname "$tauri")
work="${TMPDIR:-/tmp}/dbunitcli-help"
port="${SIDECAR_PORT:-8080}"

# Maven と同じ JDK で sidecar を動かす（~/.mavenrc は SessionStart フックが作る）
java_home=$( [ -f "$HOME/.mavenrc" ] && . "$HOME/.mavenrc"; echo "${JAVA_HOME:-}" )
java_bin="${java_home:+$java_home/bin/}java"

rm -rf "$work"
mkdir -p "$work/result"
cp -r "$repo/sample" "$work/workspace"

(cd "$repo" \
	&& mvn -q -pl sidecar -am install -DskipTests \
	&& mvn -q -pl sidecar dependency:build-classpath -Dmdep.outputFile="$work/cp.txt")

pids=()
trap 'kill "${pids[@]}" 2>/dev/null || true' EXIT

"$java_bin" -Dmicronaut.server.port="$port" \
	-Dyo.dbunit.cli.workspace="$work/workspace" \
	-Dyo.dbunit.cli.dataset.base="$work/workspace" \
	-Dyo.dbunit.cli.result.base="$work/result" \
	-cp "$(ls "$repo"/sidecar/target/dbunit-cli-sidecar-*.jar | head -1):$(cat "$work/cp.txt")" \
	yo.dbunitcli.sidecar.Application >"$work/sidecar.log" 2>&1 &
pids+=($!)

cd "$tauri"
bunx @tailwindcss/cli -i ./src/App-src.css -o ./src/App.css >/dev/null 2>&1
SIDECAR_PORT="$port" bunx vite --config screenshot/vite.config.ts >"$work/vite.log" 2>&1 &
pids+=($!)

wait_for() {
	for _ in $(seq 1 60); do
		if curl -s -o /dev/null "$1"; then
			return 0
		fi
		sleep 1
	done
	echo "起動しませんでした: $1（ログ: $work）" >&2
	exit 1
}
wait_for "http://localhost:$port/dbunit-cli/workspace/resources"
wait_for "http://localhost:1430/"

if [ "$mode" = capture ]; then
	node "$here/capture.cjs" "$tauri/public/help/images"
else
	node "$here/smoke.cjs"
fi
