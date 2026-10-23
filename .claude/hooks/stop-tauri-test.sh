#!/usr/bin/env bash
# Stop フック: tauri/ に未テストの変更があるときだけ vitest を実行する（失敗時は exit 2 で停止を止める）
# 最後に通ったときの tauri/ のツリーを .git に記録し、未変更・未コミット差分なしならスキップする
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
stamp="$(git rev-parse --absolute-git-dir 2>/dev/null)/claude-tauri-tested" || exit 0
tree="$(git rev-parse HEAD:tauri 2>/dev/null)"
if [ -z "$(git status --porcelain -- tauri)" ] && [ -n "$tree" ] && [ "$tree" = "$(cat "$stamp" 2>/dev/null)" ]; then
  exit 0
fi
cd tauri && bun install --frozen-lockfile 1>&2 && bunx vitest run 1>&2 || exit 2
if [ -z "$(git status --porcelain -- .)" ]; then
  echo "$tree" > "$stamp"
fi
exit 0
