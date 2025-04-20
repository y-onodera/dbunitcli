# 分割と分離

## レコードの分割 {#split}

テーブルのレコードを複数に分割します。

### 基本的な分割
```json
{
  "settings": [
    {
      "name": "sales",
      "split": {
        "limit": 1000,              // 分割レコード数
        "tableName": "sales_%02d",  // 分割後のテーブル名
        "prefix": "data_",          // プレフィックス
        "suffix": "_archive"        // サフィックス
      }
    }
  ]
}
```

### キーによる分割
```json
{
  "settings": [
    {
      "name": "transactions",
      "split": {
        "breakKey": [              // 分割キー
          "year",
          "month"
        ],
        "tableName": "trn_%s_%s"   // %sはキーの値で置換
      }
    }
  ]
}
```

## カラムの分離 {#separate}

特定のカラムを別テーブルに分離します。

### 基本的な分離
```json
{
  "settings": [
    {
      "name": "users",
      "separate": [
        {
          "name": "user_profiles",   // 分離先テーブル
          "keys": ["user_id"],       // 主キー（必須）
          "include": [               // 分離するカラム
            "address",
            "tel",
            "fax"
          ]
        }
      ]
    }
  ]
}
```

### データ型指定付きの分離
```json
{
  "settings": [
    {
      "name": "orders",
      "separate": [
        {
          "name": "order_details",
          "keys": ["order_id"],
          "include": ["product_id", "quantity", "price"],
          "number": ["quantity", "price"]
        }
      ]
    }
  ]
}
```

詳細は[テーブル操作](06-operations.md)を参照してください。