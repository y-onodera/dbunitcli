# dbunitcliスキーマリファレンス

dbunitcliで使用するスキーマ定義とその使用方法について説明します。

## JSONスキーマの種類

dbunitcliでは2種類のJSONスキーマを使用します：

1. テーブル操作設定
   - データの制御、結合、フィルタリングなどの操作を定義
   - FromJsonTableSeparatorsBuilderで処理
   - 詳細は[設定ファイルの構造](settings/01-structure.md)を参照

2. Excel構造定義
   - Excelファイルのテーブル構造を定義
   - FromJsonXlsxSchemaBuilderで処理
   - 詳細は[Excel構造定義](excel/schema/01-structure.md)を参照

## テーブル操作設定

設定ファイルの構造と使用方法については以下を参照してください：
- [設定ファイルの構造](settings/01-structure.md)
- [基本的な設定例](settings/02-examples.md)
- [高度な設定例](settings/03-advanced-examples.md)
- [共通設定](settings/04-common.md)
- [テーブル設定](settings/05-table.md)

## Excel構造定義

Excelファイルの構造定義については以下を参照してください：
- [基本構造](excel/schema/01-structure.md)
- [行テーブル形式](excel/schema/02-rows.md)
- [セルテーブル形式](excel/schema/03-cells.md)
- [使用例](excel/schema/04-examples.md)

## クイックスタート

1. 使用するJSONスキーマを選択
   - テーブル操作：[設定ファイルの構造](settings/01-structure.md)
   - Excel構造：[基本構造](excel/schema/01-structure.md)
2. [基本的な設定例](settings/02-examples.md)を参考に設定作成
3. より詳細な設定方法は各ドキュメントを参照