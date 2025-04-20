# テンプレートのデータ処理：基本編

generateコマンドでtxtを指定した場合のパラメータ単位別の生成例を説明します。

## record単位の処理

1行ずつのデータを処理し、個別のテキストを生成します。

### ユーザー情報の生成
```
-- データ例（users.csv）
id,name,email,role
1,John,john@example.com,admin
2,Alice,alice@example.com,user

-- テンプレート例（user_info.stg）
User Information:
ID: $row.id$
Name: $row.name$
Email: $row.email$
Role: $row.role$
-------------------

-- 実行コマンド
dbunit generate -generateType txt \
  -unit record \
  -template user_info.stg \
  -src.srcType csv -src.src users.csv

-- 生成結果
User Information:
ID: 1
Name: John
Email: john@example.com
Role: admin
-------------------
User Information:
ID: 2
Name: Alice
Email: alice@example.com
Role: user
-------------------
```

### SQL文の生成
```
-- データ例（queries.csv）
table,column,value
users,status,active
orders,type,urgent

-- テンプレート例（select.stg）
SELECT * 
FROM $row.table$
WHERE $row.column$ = '$row.value$';

-- 生成結果
SELECT * 
FROM users
WHERE status = 'active';

SELECT * 
FROM orders
WHERE type = 'urgent';
```

### 行番号の利用
```
-- テンプレート例（numbered.stg）
$rowNum + 1$. $row.name$ ($row.email$)

-- 生成結果
1. John (john@example.com)
2. Alice (alice@example.com)
```

詳細は以下を参照してください：
- [パラメータ単位](02-processing-units.md.md)
- [テンプレート構文](04-syntax.md)