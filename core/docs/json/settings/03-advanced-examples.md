# 実践的な設定例

## パターンマッチング {#pattern-matching}

テーブル名のパターンによる設定：
```json
{
  "settings": [
    {
      "pattern": {
        "string": "mst_",
        "exclude": ["mst_temp"]
      },
      "keys": ["id"],
      "exclude": ["deleted_at"]
    },
    {
      "pattern": {
        "string": "trn_"
      },
      "keys": ["id"],
      "order": ["created_at DESC"]
    }
  ]
}
```

## データ型定義 {#data-types}

一般的なデータ型の定義例：
```json
{
  "commonSettings": [
    {
      "number": {
        "id": "id",
        "user_id": "user_id",
        "order_no": "order_no",
        "amount": "amount",
        "quantity": "quantity"
      },
      "string": {
        "name": "name",
        "user_name": "user_name",
        "code": "code",
        "tel": "tel",
        "email": "email"
      },
      "boolean": {
        "is_deleted": "is_deleted",
        "has_child": "has_child",
        "should_notify": "should_notify"
      },
      "sqlFunction": {
        "current_time": "CURRENT_TIMESTAMP",
        "total_price": "ROUND(price, 2)",
        "upper_name": "UPPER(name)"
      }
    }
  ]
}
```

## 監査カラム設定 {#audit-columns}

データ監査用カラムの共通設定：
```json
{
  "commonSettings": [
    {
      "exclude": [
        "created_at",
        "updated_at",
        "created_by",
        "updated_by",
        "deleted_at",
        "deleted_by",
        "version_no"
      ],
      "sqlFunction": {
        "created_at": "CURRENT_TIMESTAMP",
        "updated_at": "CURRENT_TIMESTAMP",
        "created_by": "CURRENT_USER",
        "updated_by": "CURRENT_USER"
      }
    }
  ]
}
```

基本的な使用例は[設定例集](01-settings-examples.md)を参照してください。