# Parameterizeコマンド

## 基本機能
パラメータデータセットの内容に基づいて、指定されたコマンドを繰り返し実行します。各レコードのデータをパラメータとして、テンプレートに適用しながら処理を行います。

## 引数
* -cmd: 実行するコマンド
* -cmdParam: コマンドパラメータ
* -unit: [パラメータ単位](template/02-processing-units.md)
* -template: テンプレートファイルパス
* -ignoreFail: 失敗を無視するかどうか
* -parameterize: パラメータ化を有効にするかどうか
* param.* : パラメータデータセットの設定 - [データソース設定](../options/01-data-source.md)
* template.* : テンプレート設定 - [テンプレート設定](../options/04-template.md#templaterenderoption-template)

詳細な説明は以下を参照してください：
- [パラメータ単位の詳細](template/01-overview.md)

## 使用例
```bash
# CSVファイルの各行のデータでSQLを実行
dbunit parameterize \
  -cmd "run" -cmdParam "-scriptType sql" \
  -param.srcType csv -param.src params.csv \
  -template query.sql \
  -unit record

# 複数のデータセットで比較を実行
dbunit parameterize \
  -cmd "compare" \
  -param.srcType csv -param.src test_cases.csv \
  -templateGroup templates \
  -unit dataset