# Coreプロジェクト Boomerangモード開発ルール

このドキュメントは、Boomerang-codeモード固有の開発ルールを定義します。

`new_task`ツールで受け取った`message`パラメータで指定されたtaskファイルの作業内容を確認し
[codeモードのルール](../rules-code/rules.md)に沿って作業します
`message`パラメータでtaskファイルが指定されていない場合、作業を中断してください。
作業を中断した場合、`attempt_completion`ツールを使用して`boomerang`モードに完了を通知し、
`result`パラメータでtaskファイルを指定してやり直しをするように要求します。
作業が完了した場合はtaskファイルを`.roo/work/tasks`から`.roo/work/tasks-done`ディレクトリに移動してください
ファイルの移動が終わったら`attempt_completion`ツールを使用して`boomerang`モードに完了を通知します
作業中にユーザからtaskファイルの作業の変更を要求された場合、taskファイルを書き換え
作業完了時には変更内容を`result`パラメータを使って`boomerang`モードに伝えてください
