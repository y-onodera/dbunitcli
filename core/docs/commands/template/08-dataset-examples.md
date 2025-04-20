# テンプレートのデータ処理：応用編

dataset単位での生成例を説明します。

## dataset単位の処理

複数テーブルのデータを一括で処理します。

### 関連テーブルの処理
```
-- データ例
users.csv:
id,name
1,John
2,Alice

orders.csv:
order_id,user_id,item
101,1,Book
102,1,Pen
103,2,Notebook

-- テンプレート例（report.stg）
User Orders:
$dataSet.users.rows:{user |
User: $user.name$
Orders:$dataSet.orders.rows:{order | $if(order.user_id == user.id)$
  - [$order.order_id$] $order.item$$endif$}$
}$

-- 生成結果
User Orders:
User: John
Orders:
  - [101] Book
  - [102] Pen
User: Alice
Orders:
  - [103] Notebook
```

### 集計レポートの生成
```
-- テンプレート例（summary.stg）
Summary Report:
$dataSet.keys:{table |
$table$: $dataSet.(table).rows.length$ records}$

Total tables: $dataSet.keys.length$

-- 生成結果
Summary Report:
users: 2 records
orders: 3 records

Total tables: 2
```

詳細は以下を参照してください：
- [パラメータ単位](02-processing-units.md.md)
- [テンプレート構文](04-syntax.md)