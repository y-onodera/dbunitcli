---
description: tauri/public/helpのヘルプHTMLを更新する。画面の項目・ボタン・挙動の追加・削除・変更時に実行。
TRIGGER when: tauri/src/app/ 配下の .tsx で画面項目・ボタン・挙動を変える実装、または画面から見える core/sidecar の挙動（パス解決・保存内容・テンプレート変数等）を変える実装が完了したとき。
DO NOT TRIGGER when: 見た目のみの変更（CSS/クラス名）、タイプミス修正、外部から見える挙動が変わらないリファクタリング。
---

## 対象ファイルの対応表

| 画面（`tauri/src/app/`） | ヘルプファイル |
|---|---|
| `form/section/` の `DatasetLoadForm` / `SrcFormSection` / `DataLoadFormSection` / `SrcTypeFormSection` / `SettingFormSection` / `srctype/*` | `sections/dataset-load-form.html` |
| `form/section/ResultFormSection` / `TemplateFormSection` / `JxlsFormSection` / `JdbcFormSection` / `TraversalFormSection` | `sections/` の result-form / template / jxls / jdbc / traversal |
| `form/section/dialog/*Dialog` ほかヘルプボタンを持つ画面 | ヘルプボタンの `command`（例: `"dialogs/sql-editor"`）が指す `.html` |
| `form/GenerateForm` | `commands/generate.html` |
| `main/Header`・`startup/StartupForm`・`sidebar/*`・`form/section/element/Chooser`（ファイル選択） | `dialogs/workspace.html`（作業の流れが変わるなら `guide/getting-started.html` も） |
| ヘルプページの追加・削除 | `index.html`（全ページを載せる） |
| `footer/Footer` | `dialogs/workspace.html`（フッターの操作）、-P は `dialogs/parameters.html` |

## 更新手順

1. **変更内容を把握する** — 追加・削除・変更されたフィールド名（`element.name` / `options.xxx`）やボタン・挙動を特定する
2. **対応するヘルプを特定する** — 上の対応表から更新対象の `.html` を決める
3. **ヘルプを更新する** — 表の行の追加・削除、説明・デフォルト値の更新。挙動はコードで確かめてから書く。文体・用語・章立ては `references/style-guide.md` に従う
4. **クロスページリンクにアンカーを付けない** — 他ページへは相対パスの `page.html`（例: `../sections/jdbc.html`）のみ
5. **ディレクトリ構成を守る** — `commands/`（`commands/<cmd>/types/` に固有type）、`sections/`（`dataset-load-form/types/`=srcType、`result-form/types/`=resultType）、`dialogs/`、`reference/`、`guide/`（使い方）、ルートは `index.html` と `help.css` のみ。アプリ側の呼び出しは `helpCommand`/`command` に `public/help` からの拡張子なしパス（例: `sections/jdbc`）を指定する
6. **同一ページ内アンカーはそのまま維持する** — `<h2 id="...">` と `href="#..."` の対応を壊さない
7. **スタイルは `help.css` に集約する** — ページに `<style>` を書かず、`<link rel="stylesheet" href="（相対パス）help.css" />` で読み込む
