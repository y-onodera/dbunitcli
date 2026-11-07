---
description: tauriのダイアログの幅・高さ・ボタン配置・テキストエリア最大化の規定（ウィンドウからはみ出してボタンを押せなくなるのを防ぐ）。
TRIGGER when: tauri/src で ModalOverlay / FullDialog / SettingDialog を使うダイアログを追加、または幅・高さ・ボタン配置・入れ子構造を変えるとき。
DO NOT TRIGGER when: ダイアログ内の文言・ロジックだけの変更。
---

基準は既定ウィンドウ 800×600（`src-tauri/tauri.conf.json`）。スクロールせずにボタンを押せること。

## ボタン行
- `FullDialog` は `DialogFooter` を直下（`DialogBody` の外）、`ModalOverlay` は `DialogActions` を直下（Fragment 経由は可）に置く。sticky で下端に固定されるが、`<div>` で包むとその中でしか効かない
- `SettingDialog` は組込み。ボタン追加は `footerActions`
- スクロールは枠に任せ、本文を `overflow-y-auto` + `max-h-*` の箱で包まない
- 子ダイアログは `ModalOverlay zClass="z-modal-nested"` を親ダイアログの JSX 内に置く（`FullDialog` はトップレイヤーなので外だと隠れる）

## 幅
- `FullDialog` 本文は `DialogBody size`、`ModalOverlay` は `width` で決める。どちらも枠の `max-w-full` でウィンドウ幅に縮むので、`FullDialog` に幅クラスを足したり `max-w-*` を上書きしたりしない
- 縮まない指定（本文内の固定幅・`min-w-*`）は 600px 以下に抑える

## 高さ
- 枠の上限（`FullDialog` はブラウザ既定、`ModalOverlay` は `max-h-[80vh]`）は変えない
- 本文内のスクロール領域は1つあたり `max-h-96` 以下。`h-[…vh]` で枠を埋めない
- 編集用 `TextArea` は `DialogBody` 直下か `DialogField grow` 内に置き、枠（`SettingDialog` / `ModalOverlay`）に `maximizable` を付ける。最大化中はこの経路で残りの高さを埋める。付けたら `public/help/dialogs/common.html` の「最大化」の表に足す

## 確認
800×600 で長めのダミー内容を入れて開き、各ボタンの中心で `document.elementFromPoint` がそのボタンを返すこと。sidecar なしなら scratchpad に Vite の入口（`tauri/node_modules` を symlink、`src/App.css` と対象を絶対パスで import）を作り Playwright で開く。
