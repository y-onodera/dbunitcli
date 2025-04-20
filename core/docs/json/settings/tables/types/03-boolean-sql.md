# boolean型とsqlFunction型

## boolean型 {#boolean}

真偽値型として扱うカラムを指定します。
値はtrueまたはfalseとして出力されます。

### 使用例
```json
{
  "settings": [
    {
      "name": "users",
      "boolean": {
        "is_active": "active_flag",     // true/false
        "has_children": "child_flag",    // true/false
        "deleted": "delete_flag"         // true/false
      }
    }
  ]
}
```

### 主な用途
- フラグ項目
- ステータス
- 条件判定結果

## sqlFunction型 {#sql}

そのまま出力される文字列（SQL関数や式）を指定します。
値はクォートなしで、指定された文字列がそのまま出力されます。

### 使用例
```json
{
  "settings": [
    {
      "name": "orders",
      "sqlFunction": {
        "created_at": "CURRENT_TIMESTAMP",           // 現在時刻
        "total": "ROUND(price * 1.1, 0)",           // 計算式
        "value": "COALESCE(amount, 0)",             // NULL変換
        "upper_name": "UPPER(name)",                // 文字列関数
        "const": "'固定値'"                         // 固定文字列
      }
    }
  ]
}
```

### 主な用途
- SQL関数の直接指定
- 計算式
- 定数値
- クォートが必要な固定値
