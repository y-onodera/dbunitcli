# テンプレート機能リファレンス

dbunitcliのテンプレート機能の使用方法について説明します。

## テンプレートの基本

### パラメータ処理
データの処理方法とデータ構造について説明します：
- [処理単位の概要](02-processing-units.md)：record/table/datasetの使い分け
- [データ構造の詳細](03-data-structures.md)：各処理単位のデータ形式

### テンプレートの作成方法
StringTemplate 4（ST4）の基本的な使い方：
- [テンプレート構文](04-syntax.md)：変数参照、制御構文、関数など

## テンプレートの使用例

### テキスト生成
generateコマンドでのテンプレート活用例：
- [基本的な使用例](05-basic-examples.md)：シンプルなテキスト生成
- [レコード単位の処理](06-record-examples.md)：1行ごとの処理
- [テーブル単位の処理](07-table-examples.md)：複数行の一括処理
- [データセット単位の処理](08-dataset-examples.md)：複数テーブルの一括処理

### コマンドでの活用
- [パラメータ処理](09-parameter-examples.md)：Parameterizeコマンドでの使用
- [共通利用ガイド](10-common-usage.md)：
  - Runコマンドでの動的SQL実行
  - DataSetLoadOptionでのテンプレート使用
  - コマンド間の連携パターン

## 基本的な使い方

1. テンプレートの準備
```
-- template.stg
SELECT * FROM $tableName$
WHERE $where:{cond | $cond.column$ = '$cond.value$'}; separator=" AND "$
```

2. パラメータデータの用意
```
-- params.csv
tableName,where.column,where.value
users,status,active
```

3. コマンド実行
```bash
dbunit parameterize -cmd run \
  -param.srcType csv -param.src params.csv \
  -template template.stg \
  -unit record
```

より詳細な使い方は各セクションのドキュメントを参照してください。