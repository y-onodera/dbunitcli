# テンプレートのデータ処理：テーブル単位

> [テキストファイル生成機能](../01-txt.md)のテーブル単位での処理例です。

table単位での生成例を説明します。

## table単位の処理

テーブル全体のデータを一括で処理します。

### リスト形式の生成
```
-- データ例（users.csv）
id,name,email,role
1,John,john@example.com,admin
2,Alice,alice@example.com,user
3,Bob,bob@example.com,user

-- テンプレート例（list.stg）
User List:
$rows:{row |
[$row.id$] $row.name$ ($row.role$)
  Email: $row.email$}$

-- 生成結果
User List:
[1] John (admin)
  Email: john@example.com
[2] Alice (user)
  Email: alice@example.com
[3] Bob (user)
  Email: bob@example.com
```

### INSERT文の一括生成
```
-- テンプレート例（insert.stg）
INSERT INTO users (id, name, email, role) VALUES
$rows:{row |($row.id$, '$row.name$', '$row.email$', '$row.role$')}; separator=",\n"$;

-- 生成結果
INSERT INTO users (id, name, email, role) VALUES
(1, 'John', 'john@example.com', 'admin'),
(2, 'Alice', 'alice@example.com', 'user'),
(3, 'Bob', 'bob@example.com', 'user');
```
