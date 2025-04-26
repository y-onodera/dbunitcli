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
# パラメータファイルを使用する場合
dbunit parameterize \
  -cmd run \
  -cmdParam param_file_name \
  -unit row \
  -param.srcType csv \
  -param.src params.csv

# デフォルトテンプレートを使用する場合
dbunit parameterize \
  -cmd run \
  -template query.sql.stg \
  -unit row \
  -param.srcType csv \
  -param.src params.csv

# オプション説明
# -cmd: 実行対象のコマンド
# -cmdParam: パラメータファイル名として使用する列名
# -parameterize: パラメータファイル名をテンプレート展開するか（デフォルトtrue）
# -unit: パラメータの処理単位（row/table/dataset）
# -template: デフォルトのテンプレートファイル（cmdParamが指定された場合は無視）
# -param.srcType: パラメータデータのソース種別
# -param.src: パラメータデータのソースパス
```

## コマンド間でのパラメータ連携

```bash
# 1. テスト用SQLの生成
dbunit parameterize \
  -cmd generate \
  -unit row \
  -cmdParam query_file \
  -param.srcType csv \
  -param.src test_cases.csv

# 2. データセットの比較（エラー時も継続）
dbunit parameterize \
  -cmd compare \
  -unit dataset \
  -cmdParam file_path \
  -ignoreFail true \
  -param.srcType csv \
  -param.src results.csv
```

注意点：
- parameterizeオプションは、cmdParamで指定された列名の値をテンプレート展開するかを制御します
- unitオプションで指定された単位（row/table/dataset）ごとに処理が実行されます
- ignoreFail指定でcompareコマンドのエラー時も処理を継続できます

パラメータ単位とデータ構造の詳細は以下を参照してください：
- [パラメータ単位](02-processing-units.md)
- [データ構造](03-data-structures.md)