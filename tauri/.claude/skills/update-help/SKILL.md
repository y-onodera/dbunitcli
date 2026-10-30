---
description: tauri/public/helpのヘルプHTMLを更新する。画面の項目・ボタン・挙動の追加・削除・変更時に実行。
TRIGGER when: tauri/src/app/ 配下の .tsx で画面項目・ボタン・挙動を変える実装、または画面から見える core/sidecar の挙動（パス解決・保存内容・テンプレート変数等）を変える実装が完了したとき。
DO NOT TRIGGER when: 見た目のみの変更（CSS/クラス名）、タイプミス修正、外部から見える挙動が変わらないリファクタリング。
---

## 更新対象のページの決め方

画面とヘルプの対応はコードから調べる（一覧は持たない。画面の追加・分割ですぐ古くなるため）。

- 変えた画面（またはそれを含む画面）のヘルプボタンの `helpCommand` / `command` が指すページを更新する: `grep -rnE '(helpCommand|command)[=:] *"' src/app`
- ヘルプボタンを持たない要素（テキストボックス・その横のボタン・`srcType` ごとの項目など）は、それを含む画面のページに書く。`srcType` / `resultType` 等の値ごとの項目は、そのページ配下の `types/` に書く
- 作業の流れ（Workspace の選択から再利用まで）が変わるなら `guide/` も更新する
- ページを追加・削除したら `index.html`（全ページを載せる）も更新する

## 更新手順

1. **変更内容を把握する** — 追加・削除・変更されたフィールド名（`element.name` / `options.xxx`）やボタン・挙動を特定する
2. **対応するヘルプを特定する** — 上の「更新対象のページの決め方」で `.html` を決める
3. **ヘルプを更新する** — 表の行の追加・削除、説明・デフォルト値の更新。挙動はコードで確かめてから書き、同じページの既存の記述が実装と食い違っていれば合わせて直す。文体・用語・章立ては `references/style-guide.md` に従う
4. **クロスページリンクにアンカーを付けない** — 他ページへは相対パスの `page.html`（例: `../sections/jdbc.html`）のみ
5. **ディレクトリ構成を守る** — `commands/`（`commands/<cmd>/types/` に固有type）、`sections/`（`dataset-load-form/types/`=srcType、`result-form/types/`=resultType）、`dialogs/`、`reference/`、`guide/`（使い方）、ルートは `index.html` と `help.css` のみ。新しいページを開くヘルプボタンには、`helpCommand`/`command` に `public/help` からの拡張子なしパス（例: `sections/jdbc`）を指定する
6. **同一ページ内アンカーはそのまま維持する** — `<h2 id="...">` と `href="#..."` の対応を壊さない
7. **スタイルは `help.css` に集約する** — ページに `<style>` を書かず、`<link rel="stylesheet" href="（相対パス）help.css" />` で読み込む
