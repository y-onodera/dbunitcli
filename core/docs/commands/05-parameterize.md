# Parameterizeコマンド

## 基本機能
パラメータデータセットの内容に基づいて、指定されたコマンドを繰り返し実行します。各レコードのデータをパラメータとして、テンプレートに適用しながら処理を行います。

## 引数
* -cmd: 実行するコマンド
* -cmdParam: コマンドパラメータ
* -unit: パラメータ単位 - [詳細な設定](#パラメータ単位)
* -template: テンプレートファイルパス
* -ignoreFail: 失敗を無視するかどうか
* -parameterize: パラメータ化を有効にするかどうか
* param.* : パラメータデータセットの設定 - [データソース設定](../settings/01-data-source.md)
* template.* : テンプレート設定 - [テンプレート設定](../settings/03-database.md#templaterenderoption-template)

## パラメータ単位

### record
* データセットの各行を個別のパラメータとして処理
* パラメータ構造：
```json
{
  "rowNum": "行番号（0から開始）",
  "tableName": "テーブル名",
  "row": {
    "列名1": "値1",
    "列名2": "値2"
  }
}
```

### table
* 各テーブルを1つのパラメータとして処理
* パラメータ構造：
```json
{
  "tableName": "テーブル名",
  "rows": [
    {
      "列名1": "値1",
      "列名2": "値2"
    }
  ]
}
```

### dataset
* データセット全体を1つのパラメータとして処理
* パラメータ構造：
```json
{
  "dataSet": {
    "テーブル名1": {
      "tableName": "テーブル名1",
      "rows": [/* レコード配列 */]
    }
  }
}
```

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