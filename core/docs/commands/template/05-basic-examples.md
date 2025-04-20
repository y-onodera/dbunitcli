# テキストファイル生成の基本

generateコマンドでtypeにtxtを指定した場合のテンプレート作成方法を説明します。

## 基本的な使い方

```bash
dbunit generate -generateType txt \
  -template template.stg \
  -unit record \
  -src.srcType csv -src.src data.csv \
  -resultPath result.txt
```

## SQL生成の例

### SELECT文の生成
```
-- データ例（data.csv）
table_name,where_column,where_value
users,status,active
orders,type,pending

-- テンプレート例（select.stg）
SELECT * 
FROM $table_name$
WHERE $where_column$ = '$where_value$';

-- 生成結果
SELECT * 
FROM users
WHERE status = 'active';

SELECT * 
FROM orders
WHERE type = 'pending';
```

### INSERT文の生成
```
-- データ例（data.csv）
table,col1,col2,val1,val2
users,name,email,John,john@example.com
users,name,email,Alice,alice@example.com

-- テンプレート例（insert.stg）
INSERT INTO $table$ ($col1$, $col2$)
VALUES ('$val1$', '$val2$');

-- 生成結果
INSERT INTO users (name, email)
VALUES ('John', 'john@example.com');

INSERT INTO users (name, email)
VALUES ('Alice', 'alice@example.com');
```
詳細は以下を参照してください：
- [パラメータ単位](02-processing-units.md.md)
- [テンプレート構文](04-syntax.md)