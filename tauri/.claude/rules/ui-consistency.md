---
paths:
  - "src/**/*.tsx"
---

# UI一貫性（UIラベル・ボタン・ダイアログタイトルはすべて英語）

## ボタン

| 用途 | コンポーネント |
|------|--------------|
| 確定・保存・適用 | `BlueButton` |
| キャンセル・閉じる | `WhiteButton` |
| disabled / id が必要 | `Button` |
| アイコン+テキスト（インライン） | `ButtonWithIcon` |
| 用途固定アイコン | `EditButton`, `DeleteButton` 等 |

- ダイアログ: `BlueButton`（左）→ `WhiteButton`（右）、生の `<button>` 禁止

## Tailwind CSS
- フォーカスリング: `ring-primary-ring focus-visible:ring-3`
- 入力ボーダー: `border border-border`、背景: `bg-input`（入力専用）/ `bg-surface-subtle`（表示専用）
- 色はセマンティッククラス（`bg-primary`/`text-content`等）。パレットクラス（`bg-indigo-500`等）禁止

## テキストボックス横ボタン
- アイコン1つ: `BlueButtonIcon`系、アイコン2つ以上: `BlueSettingButton`（ドロップダウン）
- ドロップダウン内: 編集系→`EditButton`、参照系→`PreviewButton`、削除→`DeleteButton`、ファイル→`FileButton`、ディレクトリ→`DirectoryButton`

## ExpandButton caption
- `"<type> option"` 形式（英語小文字）例: `"traversal option"`
