---
description: tauri/public/helpのヘルプHTMLを更新する。フォームセクションの項目追加・削除・変更時に実行。
TRIGGER when: tauri/src/app/form/section/ 配下のコンポーネント（.tsx）の追加・削除・変更を伴う実装タスクが完了したとき。
DO NOT TRIGGER when: コンポーネントの見た目のみの変更（CSS/クラス名）、タイプミス修正、リファクタリングで外部から見えるフィールドに変更がない場合。
---

## 対象ファイルの対応表

| フォームコンポーネント | ヘルプファイル |
|---|---|
| `DatasetLoadForm.tsx` / `SrcFormSection.tsx` / `DataLoadFormSection.tsx` / `SrcTypeFormSection.tsx` / `SettingFormSection.tsx` / `srctype/*.tsx` | `sections/dataset-load-form.html` |
| `ResultFormSection.tsx` | `sections/result-form.html` |
| `TemplateFormSection.tsx` | `sections/template.html` |
| `JxlsFormSection.tsx` | `sections/jxls.html` |
| `JdbcFormSection.tsx` | `sections/jdbc.html` |
| `GenerateForm.tsx` | `commands/generate.html` |

## 更新手順

1. **変更内容を把握する** — 変更されたコンポーネントで追加・削除・変更されたフィールド名（`element.name` / `options.xxx`）を特定する
2. **対応するヘルプを特定する** — 上の対応表から更新対象の `.html` を決める
3. **ヘルプを更新する**
   - 追加フィールド: 該当セクションの表に行を追加し、型・説明・デフォルト値を記載
   - 削除フィールド: 該当行を削除
   - 変更フィールド: 説明・デフォルト値を更新
4. **クロスページリンクにアンカーを付けない** — 他ページへのリンクは相対パスの `page.html`（例: `../sections/jdbc.html`）のみ。`page.html#anchor` 形式は使用禁止
5. **ディレクトリ構成を守る** — `commands/`（各コマンド。`commands/<cmd>/types/` にそのコマンド固有のtype）、`sections/`（フォームセクション。`dataset-load-form/types/`=srcType、`result-form/types/`=resultType）、`dialogs/`、`reference/`、ルートは `index.html` のみ。アプリ側の呼び出しは `helpCommand`/`command` に `public/help` からの拡張子なしパス（例: `sections/jdbc`）を指定する
6. **同一ページ内アンカーはそのまま維持する** — `<h2 id="...">` と `href="#..."` の対応を壊さない
