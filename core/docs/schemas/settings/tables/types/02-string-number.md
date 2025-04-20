# string型とnumber型

## string型 {#string}

文字列型として扱うカラムを指定します。
値は単一引用符で囲まれて出力されます。

### 使用例
```json
{
  "settings": [
    {
      "name": "users",
      "string": {
        "name": "full_name",        // 'John Doe'
        "email": "mail_address",    // 'john@example.com'
        "code": "user_code",        // '001'
        "birth": "birth_date"       // '2023-01-01'
      }
    }
  ]
}
```

### 主な用途
- テキストデータ
- 数値形式の文字列（郵便番号など）
- 日付時刻文字列
- バイナリデータ

## number型 {#number}

数値型として扱うカラムを指定します。
値はクォートなしで出力されます。

### 使用例
```json
{
  "settings": [
    {
      "name": "orders",
      "number": {
        "id": "order_id",          // 1
        "amount": "total_amount",   // 1234.56
        "quantity": "item_count",   // 100
        "rate": "tax_rate"         // 0.08
      }
    }
  ]
}
```

### 主な用途
- 主キー（ID）
- 金額
- 数量
- 集計値
