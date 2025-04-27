# Parameterizeコマンド

## 基本機能
パラメータデータセットの内容に基づいて、指定されたコマンドを繰り返し実行します。各レコードのデータをパラメータとして、テンプレートに適用しながら処理を行います。

## 引数
* -cmd: 実行するコマンド
* param.* : パラメータデータセットの設定 - [データソース設定](../options/01-data-source.md)
* -cmdParam: パラメータファイル名として使用する列名
* -template: デフォルトのパラメータファイル（cmdParamが指定された場合は無視）
* -parameterize:  パラメータファイルをテンプレート展開するか（デフォルトtrue）
* -unit: [パラメータ単位](../options/template/01-overview.md)
* -ignoreFail: 失敗を無視するかどうか
* template.* : テンプレート設定 - [テンプレート設定](../options/04-template.md)

## 使用例
```bash
# CSVファイルの各行のデータでSQLを実行
dbunit parameterize \
  -cmd run -cmdParam query_file \
  -param.srcType csv -param.src params.csv \
  -template query.sql \
  -unit record

# 複数のデータセットで比較を実行
dbunit parameterize \
  -cmd compare \
  -param.srcType csv -param.src test_cases.csv \
  -template compare_param.txt \
  -templateGroup templates \
  -unit dataset