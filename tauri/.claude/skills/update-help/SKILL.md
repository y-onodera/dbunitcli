---
description: tauri/public/helpのヘルプHTMLを更新する。フォームセクションの項目追加・削除・変更時に実行。
TRIGGER when: tauri/src/app/form/section/ 配下のコンポーネント（.tsx）の追加・削除・変更を伴う実装タスクが完了したとき。
DO NOT TRIGGER when: コンポーネントの見た目のみの変更（CSS/クラス名）、タイプミス修正、リファクタリングで外部から見えるフィールドに変更がない場合。
---

## 対象ファイルの対応表

| フォームコンポーネント | ヘルプファイル |
|---|---|
| `DatasetLoadForm.tsx` / `SrcFormSection.tsx` / `DataLoadFormSection.tsx` / `SrcTypeFormSection.tsx` / `SettingFormSection.tsx` / `srctype/*.tsx` | `dataset-load-form.html` |
| `ResultFormSection.tsx` | `result-form.html` |
| `TemplateFormSection.tsx` | `template.html` |
| `JxlsFormSection.tsx` | `jxls.html` |
| `JdbcFormSection.tsx` | `jdbc.html` |
| `GenerateForm.tsx` | `generate.html` |

## 更新手順

1. **変更内容を把握する** — 変更されたコンポーネントで追加・削除・変更されたフィールド名（`element.name` / `options.xxx`）を特定する
2. **対応するヘルプを特定する** — 上の対応表から更新対象の `.html` を決める
3. **ヘルプを更新する**
   - 追加フィールド: 該当セクションの表に行を追加し、型・説明・デフォルト値を記載
   - 削除フィールド: 該当行を削除
   - 変更フィールド: 説明・デフォルト値を更新
4. **クロスページリンクを使わない** — 他ページへのリンクは `page.html` のみ（`page.html#anchor` 形式は使用禁止）
5. **同一ページ内アンカーはそのまま維持する** — `<h2 id="...">` と `href="#..."` の対応を壊さない
