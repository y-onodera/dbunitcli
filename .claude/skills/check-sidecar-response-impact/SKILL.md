---
description: sidecar期待値JSON変更時のフロントエンドテスト影響調査と修正案提案
TRIGGER when: `sidecar/src/test/resources/yo/dbunitcli/sidecar/controller/` 以下のJSONファイル変更時。
DO NOT TRIGGER when: 読み取りのみ、または上記パス以外の変更のみ。
---

以下を順に実施すること。

## 1. 対応フィクスチャの特定

`tauri/src/tests/app/form/fixtures.ts` を読む。命名規則: `{command}-{action}-response.json` → `{command}{Action}ResponseFixture`。

## 2. 差分調査

変更JSONとfixtures.tsを比較し、以下を特定:
- フィールドの追加・削除
- `value` / `attribute`（`type`, `required`, `selectOption`, `defaultPath`）の変更
- 共通ヘルパー（`makeCsvSrcData`等）経由の変更は全利用箇所への波及を確認

## 3. テスト影響調査

`tauri/src/tests/app/form/*.test.tsx` で変更フィクスチャを参照するテストケースを特定。

## 4. 修正案提示

以下を報告し、実際に修正するかユーザーに確認:
- `fixtures.ts` の修正箇所（コード差分）
- 影響テストケースと修正内容（テーブル形式）
