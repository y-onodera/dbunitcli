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

### テンプレート作成時の注意点
テンプレート構文内で条件分岐や書式定義を使用する場合は、適切な型定義が必要です：

- DBから取得した値はDBの型がデフォルトの型になる
- ファイルから取得した値は文字列型がデフォルトの型になる
- 数値として書式を適用する場合は、number型として定義が必要
- 条件分岐で使用する場合は、boolean型として定義が必要
- 条件分岐で式（例：price > 1000）を使用する場合は、boolean型のカラムとして式を定義

型定義の詳細は[データ型の定義](../../json/settings/tables/types/01-data-types.md)を参照

## パラメータの渡し方

テンプレートへのパラメータ渡しには2つの方法があります：

### 1. Parameterizeコマンドによる実行
CSVなどのデータファイルからパラメータを読み込んで処理を実行します：

```bash
# パラメータデータの例（params.csv）
table_name,column_name,value
users,status,active
orders,type,urgent

# 実行コマンド
dbunit parameterize -cmd run \
  -param.srcType csv -param.src params.csv \
  -template query.stg \
  -unit record
```

### 2. -Pオプションによる直接指定
コマンドラインから直接パラメータを指定します：

```bash
# パラメータを直接指定
dbunit run -scriptType sql \
  -template query.stg \
  -P table_name=users \
  -P status=active

# 複数パラメータの指定
dbunit generate -generateType txt \
  -template report.stg \
  -P date=2025-04-20 \
  -P type=summary \
  -P format=text
```

## テンプレートの使用例

### コマンドでの活用
- [共通利用ガイド](05-common-usage.md)：
  - Runコマンドでの動的SQL実行
  - DataSetLoadOptionでのテンプレート使用
  - コマンド間の連携パターン
- [パラメータ処理](06-parameter-examples.md)：Parameterizeコマンドでの使用

より詳細な使い方は各セクションのドキュメントを参照してください。