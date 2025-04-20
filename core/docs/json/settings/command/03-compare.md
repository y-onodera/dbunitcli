# Compare時の設定

Compareコマンドでの設定ファイルの使用方法を説明します。

## 基本的な比較設定

### キーによる比較
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],          // 比較キー
      "include": [             // 比較対象カラム
        "name",
        "email",
        "status"
      ]
    }
  ]
}
```

### 複合キーによる比較
```json
{
  "settings": [
    {
      "name": "order_details",
      "keys": [               // 複合キー
        "order_id",
        "line_no"
      ],
      "number": [            // 数値比較
        "quantity",
        "price"
      ]
    }
  ]
}
```

## テーブル操作との組み合わせ

### 結合してから比較
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "customers",
        "column": ["customer_id"]
      },
      "keys": ["order_id"],
      "include": [
        "customer_name",
        "total_amount"
      ]
    }
  ]
}
```

### 分割したテーブルの比較
```json
{
  "settings": [
    {
      "name": "sales",
      "split": {
        "breakKey": ["year"]
      },
      "keys": ["id"],
      "order": ["created_at"]
    }
  ]
}
```
