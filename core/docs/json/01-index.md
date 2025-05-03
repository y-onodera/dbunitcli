# dbunitcliスキーマリファレンス

dbunitcliで使用するスキーマ定義とその使用方法について説明します。

## フィルター機能

dbunitcliでは、テーブル操作設定とExcel構造定義の両方で使用できるフィルター機能を提供します：

- [機能概要](filter/01-overview.md)
- [フィルタータイプ](filter/02-filter-types.md)
- [設定例](filter/03-filter-examples.md)

## JSONスキーマの種類

dbunitcliでは2種類のJSONスキーマを使用します：

1. テーブル操作設定
   - データの制御、結合、フィルタリングなどの操作を定義
   - FromJsonTableSeparatorsBuilderで処理
   - 詳細は[設定ファイルの構造](settings/01-structure.md)を参照

2. Excel構造定義
   - Excelファイルのテーブル構造を定義
   - FromJsonXlsxSchemaBuilderで処理
   - 詳細は[Excel構造の基本](excel/02-structure.md)を参照

## テーブル操作設定

### 基本設定
- [設定ファイルの構造](settings/01-structure.md)
- [基本的な設定例](settings/02-examples.md)
- [高度な設定例](settings/03-advanced-examples.md)

### コマンド別設定
- [コマンド共通のデータ読み込み設定](settings/command/01-load.md)
- [generateコマンドの設定](settings/command/02-generate.md)
- [compareコマンドの設定](settings/command/03-compare.md)

### テーブル設定
- [テーブル名の指定](settings/tables/01-table-names.md)
- [JEXL式の使用](settings/tables/02-jexl-expressions.md)
- [カラム設定](settings/tables/03-column-settings.md)
- [データ制御](settings/tables/04-data-control.md)
- [フィルタリング](settings/tables/05-filtering.md)
- [テーブル操作](settings/tables/06-operations.md)
- [分割・分離](settings/tables/07-split-separate.md)
- [テーブル結合](settings/tables/08-table-join.md)
- [結合条件](settings/tables/09-join-conditions.md)

### データ型
- [データ型の概要](settings/tables/types/01-data-types.md)
- [文字列と数値](settings/tables/types/02-string-number.md)
- [真偽値とSQL](settings/tables/types/03-boolean-sql.md)

## Excel構造定義

### 基本構造
- [基本構造](excel/02-structure.md)
- [行テーブル形式](excel/03-rows.md)
- [セルテーブル形式](excel/04-cells.md)
- [使用例](excel/05-examples.md)

### パターンマッチング
- [パターンマッチングの基本](excel/06-pattern-matching.md)
- [パターンの種類](excel/07-pattern-types.md)
- [パターンマッチングの例](excel/08-pattern-examples.md)

## クイックスタート

1. 使用するJSONスキーマを選択
   - テーブル操作：[設定ファイルの構造](settings/01-structure.md)
   - Excel構造：[基本構造](excel/02-structure.md)
2. [基本的な設定例](settings/02-examples.md)を参考に設定作成
3. より詳細な設定方法は各ドキュメントを参照