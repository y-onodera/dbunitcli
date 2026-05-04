---
description: 実装後の品質確認。変更ファイルに /simplify を適用し、tauri変更時はvitestでテストを確認する。
TRIGGER when: コードファイル（.ts, .tsx, .js, .jsx, .java, .rs 等）またはpom.xmlの変更を伴うタスクの**全編集が完了した時点で1回**。
DO NOT TRIGGER when: 読み取りのみ、質問への回答のみ、計画作成のみ、コード未変更時。/simplify または /post-impl 実行中の個別Edit後。
---

以下の順で実行すること。

1. 変更・追加・修正されたすべてのコードファイルに対して `/simplify` を実行する。
2. `tauri/` 配下のファイルが変更対象に含まれる場合、`bun vitest run` を `tauri/` ディレクトリで実行してテストがすべて通ることを確認する。
3. `CLAUDE.md` / `.claude/rules/*.md` / `.claude/skills/*/SKILL.md` が変更対象に含まれる場合、`/review-md` を実行する。
