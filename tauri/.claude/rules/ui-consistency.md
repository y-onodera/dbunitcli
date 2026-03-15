---
paths:
  - "src/**/*.tsx"
---

# UI 一貫性ルール

## 言語
- **UIラベル・ボタン・ダイアログタイトルはすべて英語**

## ボタンコンポーネント
| 用途 | コンポーネント |
|------|--------------|
| 確定・保存・適用 | `BlueButton` |
| キャンセル・閉じる | `WhiteButton` |
| disabled / id が必要 | `Button`（props で条件分岐） |
| アイコン+テキスト（インライン） | `ButtonWithIcon` |
| 用途固定アイコン | `EditButton`, `DeleteButton` 等 |

- ダイアログ: `BlueButton`（左）→ `WhiteButton`（右）の順
- 生の `<button>` は使わない。必ずコンポーネントを使う

## Tailwind CSS
- フォーカスリングは必ず `ring-indigo-300 focus-visible:ring-3`（全インタラクティブ要素）
- インプット系のボーダー: `border border-gray-300`、背景: `bg-gray-50`
- disabled ボタン: `bg-gray-200 text-gray-400 border-gray-200 cursor-not-allowed`（`Button` の props で渡す）
- コンポーネントが提供するスタイルに手を加えない。カスタムが必要なら `Button` の props を使う

## テキストボックス横のアイコンボタン

- 配色は**グレーの IconButton 系**（`EditButton`, `PreviewButton`, `FileButton`, `SettingButton` 等）で統一
- `ButtonWithIcon`（青背景）はテキストボックス横には使わない
- ボタン並び順: [ファイル/プレビュー/編集系] → [削除系（`RemoveButton`）]（削除は常に末尾）

### アイコンボタンの用途別コンポーネント

| 用途 | コンポーネント | 例 |
|------|--------------|-----|
| 複数の選択肢を持つドロップダウンメニュー | `SettingButton` | CommandFormElement の DropDownMenu |
| ダイアログを起動（編集・ビルダー系） | `EditButton` | JdbcUrlBuilderButton, TemplatePreviewButton |
| ダイアログを起動（プレビュー・参照系） | `PreviewButton` | JdbcPropertiesPreviewButton |
| ファイル選択ダイアログ | `FileButton` | FileChooser の FileButton |
| ディレクトリ選択ダイアログ | `DirectoryButton` | DirectoryChooser の DirectoryButton |
| リソース削除 | `RemoveButton` 系 | RemoveDatasetSettingButton 等 |

## ExpandButton の caption
- パターン: `"<type> option"`（英語小文字）
- 例: `"traversal option"`, `"table option"`, `"csv option"`
