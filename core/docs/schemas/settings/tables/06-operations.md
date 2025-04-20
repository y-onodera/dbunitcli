# テーブル操作

テーブルの分割、結合などの操作方法を説明します。
詳細な使用方法は各操作のドキュメントを参照してください。

## 操作の種類

### 1. レコードの分割（split）
- レコード数による分割
- キーによる分割
- テーブル名の制御
詳細は[分割と分離](07-split-separate.md#split)を参照

### 2. カラムの分離（separate）
- 特定カラムの別テーブル化
- 主キーの継承
- データ型の指定
詳細は[分割と分離](07-split-separate.md#separate)を参照

### 3. テーブルの結合
- 内部結合（innerJoin）
- 外部結合（outerJoin）
- 完全結合（fullJoin）
詳細は[テーブル結合](08-table-join.md)を参照

## 基本的な使用例

```json
{
  "settings": [
    {
      // レコード分割
      "name": "sales",
      "split": {
        "limit": 1000,
        "breakKey": ["year", "month"]
      }
    },
    {
      // カラム分離
      "name": "users",
      "separate": [
        {
          "name": "user_profiles",
          "keys": ["user_id"],
          "include": ["address", "tel"]
        }
      ]
    },
    {
      // テーブル結合
      "name": "orders",
      "innerJoin": {
        "left": "orders",
        "right": "customers",
        "column": ["customer_id"]
      }
    }
  ]
}