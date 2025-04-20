# カラム制御の基本

テーブル設定での基本的なカラム制御の方法を説明します。
詳細な制御方法は各セクションのドキュメントを参照してください。

## 基本的なカラム指定

### 主キーの指定
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"]            // 単一キー
    },
    {
      "name": "orders",
      "keys": [                 // 複合キー
        "order_id",
        "line_no"
      ]
    }
  ]
}
```

### 対象カラムの制御
```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],
      "include": [             // 処理対象
        "id",
        "name",
        "email"
      ],
      "exclude": [            // 除外対象
        "password",
        "deleted_at"
      ]
    }
  ]
}
```

## 動的なカラム値の生成

各データ型でJExlの式を使用して動的にカラム値を生成できます：

```json
{
  "settings": [
    {
      "name": "users",
      "string": {
        "full_name": "firstName + ' ' + lastName",
        "formatted_date": "created_at.substring(0,10)"
      },
      "number": {
        "age_days": "DATEDIFF(day, birth_date, CURRENT_DATE)",
        "total": "quantity * unit_price"
      },
      "boolean": {
        "is_adult": "age >= 20",
        "has_discount": "total > 10000"
      },
      "sqlFunction": {
        "updated_timestamp": "CURRENT_TIMESTAMP",
        "upper_name": "UPPER(name)"
      }
    }
  ]
}
```

式の中では行のカラム値を変数として参照できます。
使用可能な式の構文については[JExl式の利用](02-jexl-expressions.md)を参照してください。

## カラム指定のルール

1. includeとexclude
   - includeのみ：指定カラムのみ処理
   - excludeのみ：指定カラム以外を処理
   - 両方指定：excludeが優先

2. カラム名の解決
   - 大文字小文字は区別しない
   - スキーマ名は含めない
   - 存在しないカラムは無視

カラム制御の詳細については以下を参照してください：
- [データ型の定義](types/01-data-types.md)