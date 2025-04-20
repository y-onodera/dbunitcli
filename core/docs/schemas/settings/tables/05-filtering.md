# データのフィルタリング

テーブルデータのフィルタリング機能について説明します。

## 基本的なフィルタリング

filter配列でWHERE句に相当する条件を指定します：
```json
{
  "settings": [
    {
      "name": "orders",
      "filter": [
        "amount > 0",
        "status != 'CANCELED'"
      ]
    }
  ]
}
```

## フィルター式の使用

フィルター条件にはJExl式を使用します：
```json
{
  "settings": [
    {
      "name": "transactions",
      "filter": [
        "amount > 1000 && status == 'APPROVED'",
        "type != null && type.startsWith('SALE')",
        "created_at >= '2024-01-01' || priority == 'HIGH'"
      ]
    }
  ]
}
```

使用可能な式の構文については[JExl式の利用](02-jexl-expressions.md)を参照してください。

## 複雑な条件

### 複数条件の組み合わせ
複数のfilter条件はAND条件で結合されます：
```json
{
  "settings": [
    {
      "name": "users",
      "filter": [
        "age >= 20",                    // 条件1
        "status == 'ACTIVE'",           // AND 条件2
        "last_login >= '2024-01-01'"    // AND 条件3
      ]
    }
  ]
}
```

### OR条件の使用
OR条件を使用する場合は単一の式の中で || 演算子を使用：
```json
{
  "settings": [
    {
      "name": "payments",
      "filter": [
        "status == 'FAILED' || amount > 10000",  // 単一式でOR条件
        "created_at >= '2024-01'"                // AND 条件
      ]
    }
  ]
}
```

## 使用上の注意点

1. 複数の条件は読みやすさのため分割することを推奨
2. 複雑なOR条件は単一の式にまとめる
3. フィルター条件は必要最小限にする
