# Generate時の設定

Generateコマンドでテーブル設定を生成する方法を説明します。

## 基本的な使用方法

```bash
dbunit generate -generateType settings -includeAllColumns true
```

## 生成される設定

### シンプルな設定（デフォルト）
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"]
    }
  ]
}
```

### 詳細な設定（includeAllColumns=true）
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],
      "include": [
        "id",
        "name",
        "email",
        "created_at"
      ],
      "string": {
        "name": "name",
        "email": "email"
      },
      "number": {
        "id": "id"
      },
      "sqlFunction": {
        "created_at": "CURRENT_TIMESTAMP"
      }
    }
  ]
}
```

## 型の推定ルール

JDBCメタデータから以下のように型を推定：

1. 文字列型（string）
   - VARCHAR
   - CHAR
   - TEXT
   - TIMESTAMP
   - DATETIME
   - その他の文字列型

2. 数値型（number）
   - INTEGER
   - DECIMAL
   - NUMERIC
   - FLOAT
   - その他の数値型
