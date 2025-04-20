# 結合条件の指定

テーブル結合時の条件指定方法について説明します。

## 単純な結合

### 単一キーでの結合
最も基本的な結合方法：
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "items",
        "column": ["item_id"]
      }
    }
  ]
}
```

### 複合キーでの結合
複数のカラムで結合する場合：
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "items",
        "column": [
          "order_id",
          "item_code"
        ]
      }
    }
  ]
}
```

## 式による結合

より複雑な条件でテーブルを結合する場合は"on"パラメータでJExl式を使用できます：
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "items",
        "on": "orders_item_id == items_id && orders_status != 'CANCELED'"
      }
    }
  ]
}
```

カラムは"{テーブル名}_{カラム名}"の形式で参照します。
式の詳しい構文については[JExl式の利用](02-jexl-expressions.md)を参照してください。

## 使用上の注意点

1. column指定とon指定は排他的です
2. 結合条件は必ず指定してください
3. 複雑な条件は可読性のため分割を検討してください

詳細は以下を参照してください：
- [テーブル結合の基本](08-table-join.md)
- [JExl式の利用](02-jexl-expressions.md)