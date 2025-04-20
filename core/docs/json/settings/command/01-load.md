# データソースでの設定

データ読み込み時のテーブル設定の使用方法を説明します。

## カラム型の指定

### 基本的な型指定
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],
      "string": [           // 文字列として読み込み
        "name",
        "email",
        "created_at"
      ],
      "number": [          // 数値として読み込み
        "id",
        "age",
        "point"
      ]
    }
  ]
}
```

### データ変換指定
```json
{
  "settings": [
    {
      "name": "orders",
      "keys": ["order_id"],
      "sqlFunction": [     // SQLとして出力
        "CURRENT_TIMESTAMP",
        "ROUND(price, 0)",
        "'固定値'"
      ]
    }
  ]
}
```

## テーブル名の制御

### 共通プレフィックス/サフィックス
```json
{
  "settings": [
    {
      "name": "customers",
      "prefix": "staging_",  // staging_customers
      "keys": ["id"]
    },
    {
      "name": "orders",
      "suffix": "_new",     // orders_new
      "keys": ["order_id"]
    }
  ]
}
```