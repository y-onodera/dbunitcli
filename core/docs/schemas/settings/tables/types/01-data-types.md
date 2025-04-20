# データ型の定義

テーブル設定で使用できるデータ型とその出力形式について説明します。
より詳細な説明は各型のドキュメントを参照してください。

## 基本型の一覧

| 型 | 説明 | 出力形式 | 詳細 |
|---|---|---|---|
| string | 文字列型 | 'value' | [文字列型](02-string-number.md#string) |
| number | 数値型 | value | [数値型](02-string-number.md#number) |
| boolean | 真偽値型 | true/false | [真偽値型](03-boolean-sql.md#boolean) |
| sqlFunction | SQL関数 | そのまま | [SQL関数](03-boolean-sql.md#sql) |

## 基本的な使用例

```json
{
  "settings": [
    {
      "name": "users",
      "keys": ["id"],
      // 型の指定
      "string": {
        "name": "first_name",
        "email": "email_address"
      },
      "number": {
        "id": "user_id",
        "age": "birth_date"
      },
      "boolean": {
        "is_active": "status"
      },
      "sqlFunction": {
        "created_at": "CURRENT_TIMESTAMP",
        "upper_name": "UPPER(name)"
      }
    }
  ]
}
```

## 動的な値の生成

各型ではJExlの式を使用して値を動的に生成することもできます。
詳しくは[カラム値の生成](../03-column-settings.md#動的なカラム値の生成)を参照してください。

## 型の選択基準

1. string型
   - テキストデータ
   - 日付時刻（文字列として扱う場合）
   - バイナリデータ

2. number型
   - 整数値
   - 小数値
   - 集計対象の値

3. boolean型
   - フラグ項目
   - 真偽値

4. sqlFunction
   - SQL関数の直接指定
   - カスタム式
   - 定数値
