# テーブル設定スキーマのドキュメント構成

## ドキュメントの分割方針

### 1. 基本構造 (01-table-settings.md)
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["user_id"]
    }
  ],
  "commonSettings": [
    {
      "exclude": ["created_at", "updated_at"]
    }
  ],
  "import": [
    {
      "path": "./common/base_settings.json"
    }
  ]
}
```

### 2. データ型 (02-data-types.md)
```json
{
  "settings": [
    {
      "name": "sales",
      "keys": ["id"],
      // 文字列型（クォート付きで出力）
      "string": [
        "customer_name",
        "email"
      ],
      // 数値型（クォートなしで出力）
      "number": [
        "amount",
        "quantity"
      ],
      // 真偽値（true/falseで出力）
      "boolean": [
        "is_active"
      ],
      // そのまま出力する文字列
      "sqlFunction": [
        "CURRENT_TIMESTAMP",
        "ROUND(price, 2)",
        "UPPER(name)"
      ]
    }
  ]
}
```

### 3. カラム制御 (03-column-settings.md)
```json
{
  "settings": [
    {
      // テーブル名の指定
      "name": "orders",
      // または
      "name": ["orders", "order_details"],
      // または
      "pattern": {
        "string": "order_*",
        "exclude": ["order_tmp"]
      },

      // カラムの制御
      "keys": ["order_id", "line_no"],
      "include": ["customer_id", "product_id", "quantity"],
      "exclude": ["deleted_at"],
      "order": ["order_date DESC", "customer_id"],
      "filter": ["quantity > 0"],
      "distinct": true
    }
  ]
}
```

### 4. テーブル操作 (04-table-operations.md)
```json
{
  "settings": [
    {
      // レコード分割
      "name": "sales",
      "split": {
        "limit": 1000,
        "breakKey": ["year", "month"],
        "tableName": "sales_%02d",
        "prefix": "data_",
        "suffix": "_archive"
      },

      // カラム分離
      "separate": [
        {
          "name": "customer_details",
          "keys": ["customer_id"],
          "include": ["name", "email", "address"]
        }
      ],

      // テーブル結合
      "innerJoin": {
        "left": "orders",
        "right": "customers",
        "column": ["customer_id"]
      }
      // または outerJoin/fullJoin
    }
  ]
}
```

## 実装方針
1. 実際の設定例を中心に説明
2. 使用目的と効果を明記
3. オプションの組み合わせ例を提示

## レビュー基準
1. 設定例の実用性
2. 説明の分かりやすさ
3. オプションの網羅性
4. 実装との整合性