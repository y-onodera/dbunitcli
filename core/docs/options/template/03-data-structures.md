# パラメータ単位のデータ構造

各パラメータ単位で利用可能なデータ構造について説明します。

## record単位の構造

1行のデータに対してテンプレートを適用します。

```
-- データ例（users.csv）
id,name,email
1,John,john@example.com
2,Alice,alice@example.com

-- データ構造
{
  "rowNum": 0,              // 行番号（0始まり）
  "tableName": "users",     // データソース名
  "row": {                  // 1行分のデータ
    "id": "1",
    "name": "John",
    "email": "john@example.com"
  }
}

-- 参照方法
$rowNum$          // 行番号
$tableName$       // テーブル名
$row.id$          // カラムの値
$row.name$
```

## table単位の構造

テーブル全体のデータを一括で処理します。

```
-- データ例（users.csv）
id,name,email
1,John,john@example.com
2,Alice,alice@example.com

-- データ構造
{
  "tableName": "users",     // データソース名
  "rows": [                 // 全行のデータ
    {
      "id": "1",
      "name": "John",
      "email": "john@example.com"
    },
    {
      "id": "2",
      "name": "Alice",
      "email": "alice@example.com"
    }
  ]
}

-- 参照方法
$tableName$                    // テーブル名
$rows.length$                  // 行数
$rows.first.id$               // 最初の行
$rows:{row | $row.name$}$     // 全行の処理
```

## dataset単位の構造

複数テーブルのデータを一括で処理します。

```
-- データ例
users.csv:
id,name
1,John
2,Alice

emails.csv:
user_id,email
1,john@example.com
2,alice@example.com

-- データ構造
{
  "dataSet": {
    "users": {              // テーブル名
      "tableName": "users",
      "rows": [
        {"id": "1", "name": "John"},
        {"id": "2", "name": "Alice"}
      ]
    },
    "emails": {
      "tableName": "emails",
      "rows": [
        {"user_id": "1", "email": "john@example.com"},
        {"user_id": "2", "email": "alice@example.com"}
      ]
    }
  }
}

-- 参照方法
$dataSet.users.tableName$     // テーブル名
$dataSet.users.rows$          // テーブルの全行
$dataSet.keys$                // テーブル名の一覧
```

パラメータ単位の概要は[パラメータ単位](02-processing-units.md)を、
テンプレートの構文は[テンプレート構文](04-syntax.md)を参照してください。