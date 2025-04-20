# テーブル結合

テーブルの結合方法の基本について説明します。
結合条件の詳細は[結合条件の指定](09-join-conditions.md)を参照してください。

## 結合の種類

### 内部結合（innerJoin）
両方のテーブルに一致するレコードのみ取得：
```json
{
  "settings": [
    {
      "name": "orders",
      "innerJoin": {
        "left": "orders",            // 左テーブル
        "right": "customers",        // 右テーブル
        "column": ["customer_id"]    // 結合キー
      }
    }
  ]
}
```

### 外部結合（outerJoin）
左テーブルの全レコードと一致する右テーブルのレコードを取得：
```json
{
  "settings": [
    {
      "name": "users",
      "outerJoin": {
        "left": "users",
        "right": "user_profiles",
        "column": ["user_id"]
      }
    }
  ]
}
```

### 完全結合（fullJoin）
両方のテーブルの全レコードを取得：
```json
{
  "settings": [
    {
      "name": "employees",
      "fullJoin": {
        "left": "employees",
        "right": "departments",
        "column": ["dept_id"]
      }
    }
  ]
}
```

## 結合後の操作
結合したテーブルに対して通常の操作が可能：
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "customers",
        "column": ["customer_id"]
      },
      "include": ["order_id", "customer_name"],
      "order": ["order_date DESC"]
    }
  ]
}
```

詳細は以下を参照してください：
- [結合条件の指定](09-join-conditions.md)
- [テーブル操作](06-operations.md)