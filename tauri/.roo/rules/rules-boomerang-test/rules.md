# Coreプロジェクト Boomerangモード開発ルール

このドキュメントは、Boomerang-testモード固有の開発ルールを定義します。

`new_task`ツールで受け取った`message`パラメータで指定されたtaskファイルの作業内容を確認し
[testモードのルール](../rules-test/rules.md)に沿って作業します
`message`パラメータでtaskファイルが指定されていない場合、作業を中断してください。
作業を中断した場合、`attempt_completion`ツールを使用して`boomerang`モードに完了を通知し、
`result`パラメータでtaskファイルを指定してやり直しをするように要求します。
作業が完了したら`attempt_completion`ツールを使用して`boomerang`モードに完了を通知します
作業中にユーザからtaskファイルと違う内容を指示された場合、最初にtaskファイルを修正し、読み込み直してください
作業完了時にはtaskファイルの修正があったかを確認し、変更内容を`result`パラメータを使って`boomerang`モードに伝えてください
