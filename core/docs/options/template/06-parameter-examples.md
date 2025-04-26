# Parameterizeコマンドのテンプレート例

## コマンドオプションの生成

### 基本的な使用例
```
-- データ例（params.csv）
test_id,table_name,operation
001,users,INSERT
002,orders,UPDATE

-- テンプレート例（options.stg）
-operation $operation$ \
-src.srcType table \
-src.src "$table_name$" \
-resultPath "result_$test_id$.sql"

-- 実行コマンド
dbunit parameterize -cmd generate -unit record \
  -param.srcType csv -param.src params.csv \
  -template options.stg

-- 生成されるコマンド
# 1回目の実行
generate -operation INSERT \
  -src.srcType table \
  -src.src "users" \
  -resultPath "result_001.sql"

# 2回目の実行
generate -operation UPDATE \
  -src.srcType table \
  -src.src "orders" \
  -resultPath "result_002.sql"
```

### データソース設定の生成
```
-- データ例（db_config.csv）
env,host,port,database
dev,localhost,3306,testdb
stg,192.168.1.10,3306,stagingdb

-- テンプレート例（jdbc.stg）
-jdbc.url "jdbc:mysql://$host$:$port$/$database$" \
-jdbc.user "$env$_user" \
-jdbc.password "$env$_pass"

-- 生成されるコマンド
-jdbc.url "jdbc:mysql://localhost:3306/testdb" \
-jdbc.user "dev_user" \
-jdbc.password "dev_pass"
```

### テスト実行設定の生成
```
-- データ例（test_cases.csv）
name,input,expect
新規登録,data/new_user.xlsx,expect/create_user.xlsx
更新処理,data/update_user.xlsx,expect/update_user.xlsx

-- テンプレート例（test.stg）
-src.srcType xlsx \
-src.src "$input$" \
-dst.srcType xlsx \
-dst.src "$expect$" \
-reportPath "report_$name$.html"

-- 生成されるコマンド
-src.srcType xlsx \
-src.src "data/new_user.xlsx" \
-dst.srcType xlsx \
-dst.src "expect/create_user.xlsx" \
-reportPath "report_新規登録.html"
```

詳細な構文は[テンプレート構文](04-syntax.md)を参照してください。
パラメータ単位については[パラメータ単位](02-processing-units.md)を参照してください。