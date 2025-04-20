# 設定例集

テーブル設定の具体的な使用例を紹介します。
より詳細な例は[実践的な設定例](01-settings-examples-advanced.md)を参照してください。

## シンプルな設定

単一テーブルの基本的な設定：
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],
      "include": ["id", "name", "email"]
    }
  ]
}
```

## 複数テーブルの設定

共通設定を使用した複数テーブルの設定：
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"]
    },
    {
      "name": "orders",
      "keys": ["order_id"]
    }
  ],
  "commonSettings": [
    {
      "exclude": ["created_at", "updated_at"],
      "number": {
        "id": "id",
        "order_id": "order_id",
        "amount": "amount"
      },
      "string": {
        "name": "name",
        "email": "email"
      }
    }
  ]
}
```

## 設定の分割と参照

外部ファイルの参照による設定の分割：
```json
{
  "settings": [
    {
      "name": "orders",
      "keys": ["order_id"],
      "include": ["order_id", "user_id", "amount"]
    }
  ],
  "import": [
    {
      "path": "./common/data_types.json"
    },
    {
      "path": "./tables/users.json"
    }
  ]
}
```

より実践的な使用例については以下を参照してください：
- [パターンマッチングの例](01-settings-examples-advanced.md#pattern-matching)
- [データ型定義の例](01-settings-examples-advanced.md#data-types)
- [監査カラムの設定例](01-settings-examples-advanced.md#audit-columns)