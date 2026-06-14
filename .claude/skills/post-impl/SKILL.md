---
description: 実装後の品質確認。/simplify適用 → Java変更時はMavenテスト → tauri変更時はtsc+vitest。
TRIGGER when: コードファイル（.ts, .tsx, .js, .jsx, .java, .rs 等）またはpom.xmlの変更を伴うタスクの**全編集が完了した時点で1回**。
DO NOT TRIGGER when: 読み取りのみ、質問への回答のみ、計画作成のみ、コード未変更時。/simplify または /post-impl 実行中の個別Edit後。
---

以下の順で実行すること。

## 1. コード品質

変更・追加・修正されたすべてのコードファイルに対して `/simplify` を実行する。

## 2. Java テスト（core/sidecar 変更時）

| 変更モジュール | コマンド |
|---|---|
| `core/` | `mvn test -pl core -am` |
| `sidecar/` | `mvn test -pl sidecar -am` |
| 両方 | `mvn test -pl core,sidecar -am` |

失敗時は原因を修正してから次のステップに進む。

## 3. フロントエンドテスト（tauri/ 変更時）

1. `bunx tsc --noEmit`（`tauri/` ディレクトリで実行）— エラーがあれば修正
2. `bun vitest run`（`tauri/` ディレクトリで実行）— 全テスト通過を確認
