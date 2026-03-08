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

## ExpandButton の caption
- パターン: `"<type> option"`（英語小文字）
- 例: `"traversal option"`, `"table option"`, `"csv option"`
