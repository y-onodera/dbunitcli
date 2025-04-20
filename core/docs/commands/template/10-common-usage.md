# テンプレート機能の共通利用

各コマンドでのテンプレート機能の使用方法について説明します。

## Runコマンドでのテンプレート使用

### SQLスクリプトのテンプレート化

```
-- データ例（params.csv）
user_id,status
1,active
2,inactive

-- テンプレート例（query.sql.stg）
SELECT * FROM users
WHERE user_id = $user_id$
  AND status = '$status$'

-- 実行コマンド
dbunit run -scriptType sql \
  -template query.sql.stg \
  -P user_id=1 -P status=active
```

### パラメータの受け渡し

1. コマンドラインから
```bash
dbunit run -scriptType sql \
  -template query.sql.stg \
  -P param1=value1 -P param2=value2
```

2. Parameterizeコマンドから
```bash
dbunit parameterize -cmd run \
  -cmdParam "-scriptType sql" \
  -param.srcType csv -param.src params.csv \
  -template query.sql.stg
```

## DataSetLoadOptionでのテンプレート使用

### データ読み込み時のテンプレート処理

```
-- データソース設定例
{
  "src": {
    "srcType": "template",
    "template": "query.sql.stg",
    "params": {
      "table": "users",
      "status": "active"
    }
  }
}
```

### 動的なSQL生成

```
-- テンプレート例（query.sql.stg）
SELECT *
FROM $table$
$if(where)$
WHERE $where:{cond | $cond.column$ = '$cond.value$'}; separator=" AND "$
$endif$

-- パラメータ例（params.json）
{
  "table": "users",
  "where": [
    {"column": "status", "value": "active"},
    {"column": "type", "value": "admin"}
  ]
}
```

## コマンド間でのパラメータ連携

```bash
# 1. パラメータを使ってSQL生成
dbunit parameterize \
  -cmd run -cmdParam "-scriptType sql" \
  -param.srcType csv -param.src params.csv \
  -template query.sql.stg

# 2. 生成したSQLを使って比較
dbunit parameterize \
  -cmd compare \
  -param.srcType csv -param.src test_cases.csv \
  -src.template query.sql.stg
```

パラメータ単位とデータ構造の詳細は以下を参照してください：
- [パラメータ単位](02-processing-units.md)
- [データ構造](03-data-structures.md)