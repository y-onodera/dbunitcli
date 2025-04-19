# Generateコマンド

## 基本機能
テンプレートを使用してファイルを生成します。SQL、設定ファイル、テキストファイル、Excelファイルなど、様々な形式のファイル生成に対応しています。データセットの内容に基づいて動的にファイルを生成できます。

## 引数
* -generateType: 生成タイプ(txt/xlsx/xls/settings/sql)
* -unit: パラメータ化の単位
* -template: テンプレートファイルパス
* -resultDir: 出力先ディレクトリ
* -resultPath: 出力ファイルパス
* -outputEncoding: 出力エンコーディング
* src.* : ソースデータセットの設定 - [データソース設定](../settings/01-data-source.md)
* template.* : テンプレート設定 - [テンプレート設定](../settings/03-database.md#templaterenderoption-template)

## 生成タイプ別の設定

### txt: テキストファイル生成
* -template: テンプレートファイルパス
* -unit: パラメータ単位を指定して生成

### xlsx/xls: Excelファイル生成
* -template: テンプレートファイルパス
* -unit: パラメータ単位（record/table/dataset）
* template.formulaProcess: Excel数式処理の有効化（xlsxのみ）

### settings: 設定ファイル生成
* 固定テンプレート使用
* -unit: datasetに固定
* src.useJdbcMetaData: 自動的に'true'に設定
* src.loadData: 自動的に'false'に設定

### sql: SQLファイル生成
* 固定テンプレート使用
* -unit: tableに固定
* -operation: SQL操作タイプ（INSERT/DELETE/UPDATE/CLEAN_INSERT/DELETE_INSERT）
* -commit: コミット有無（デフォルト: true）
* -sqlFilePrefix: 生成SQLファイル名のプレフィックス
* -sqlFileSuffix: 生成SQLファイル名のサフィックス
* src.useJdbcMetaData: 自動的に'true'に設定

## 使用例
```bash
# テーブルデータからSQL生成
dbunit generate -generateType sql \
  -operation INSERT \
  -src.srcType table -src.src "users" \
  -jdbc.url "jdbc:mysql://localhost/testdb" \
  -resultDir ./sql

# テンプレートを使用してExcelファイル生成
dbunit generate -generateType xlsx \
  -template template.xlsx \
  -src.srcType csv -src.src data.csv \
  -resultPath output.xlsx