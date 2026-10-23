#!/usr/bin/env bash
# PostToolUse フック: tauri/ の .ts/.tsx を Edit/Write したら biome check --write（import整理・format・安全なlint修正）をかける
# 自動修正できない指摘が残ったら exit 2 で Claude に返す（ローカル・web 共通。jq が無い環境もあるため JSON は bun で読む）
cd "${CLAUDE_PROJECT_DIR:-.}/tauri" || exit 0
file="$(bun -e 'const input = JSON.parse(await Bun.stdin.text()); process.stdout.write(input.tool_input?.file_path ?? "")')" || exit 0
file="${file//\\//}"
case "$file" in
  */tauri/node_modules/*) exit 0 ;;
  */tauri/*.ts | */tauri/*.tsx) ;;
  *) exit 0 ;;
esac
bunx biome check --write --no-errors-on-unmatched --error-on-warnings "$file" 1>&2 || exit 2
