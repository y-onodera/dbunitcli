---
description: sidecar期待値JSON変更時のフロントエンドテスト影響調査と修正案提案
TRIGGER when: `sidecar/src/test/resources/yo/dbunitcli/sidecar/controller/` 以下のJSONファイル変更時。
DO NOT TRIGGER when: 読み取りのみ、または上記パス以外の変更のみ。
---

## 手動実行時チェック

直前の履歴を振り返りTRIGGER条件を満たしていたか確認し、未起動の理由を報告後、通常処理を続行する。

## 通常処理

### 1. 対応フィクスチャの特定

`tauri/src/tests/app/form/fixtures.ts` を読む。変更JSONのファイル名から対応するエクスポート名を特定する（命名規則: `{command}-{action}-response.json` → `{command}{Action}ResponseFixture`）。

### 2. 差分調査

変更JSONとfixtures.tsのフィクスチャを比較し、以下を特定する:
- フィールドの追加・削除
- `value` / `attribute`（`type`, `required`, `selectOption`, `defaultPath`）の変更
- 共通ヘルパー関数（`makeCsvSrcData` 等）経由の変更は全利用箇所への波及を確認

### 3. テスト影響調査

`tauri/src/tests/app/form/` 以下の `*.test.tsx` で変更フィクスチャを参照するテストケースを特定し、フィールド存在確認・値確認・属性確認への影響を調べる。

### 4. 修正案提示

以下を報告し、実際に修正するかユーザーに確認する:
- `fixtures.ts` の修正箇所（コード差分）
- 影響テストケースと修正内容（テーブル形式）
