# 設定ファイル生成

## 概要
データベースのテーブル構造に基づいて、テーブルごとの設定ファイルを生成します。
主キー情報やカラム情報を含むJSON形式の設定ファイルを出力します。

## 設定項目
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | settings を指定 | ○ |
| -unit | dataset を指定（固定） | ○ |
| -result | 出力先ディレクトリ | ○ |
| -resultPath | 出力ファイルパス | - |
| -includeAllColumns | カラム情報の出力制御（true/false） | - |

※ 以下の設定は自動的に有効になります：
```
src.useJdbcMetaData=true  # テーブル構造の取得
src.loadData=false        # データ本体は不要
```

## 生成される成果物

### 出力ファイル
- 形式：JSON（UTF-8）
- 出力先：-resultPathまたは-resultで指定したパス

### 生成内容
#### includeAllColumns=false（デフォルト）
```json
{
  "name": "テーブル名",
  "keys": ["主キーカラム名"]
}
```

#### includeAllColumns=true
```json
{
  "name": "テーブル名",
  "keys": ["主キーカラム名"],
  "include": ["全カラムのリスト"],
  "exclude": [],
  "string": {
    "カラム名": "カラム名"
  },
  "number": {
    "カラム名": "カラム名"
  },
  "boolean": {},
  "sqlFunction": {}
}
```

## 使用例
```bash
# 主キー情報のみ出力
dbunit generate -generateType settings \
  -unit dataset \
  -src.srcType table -src.src tables.txt \
  -resultDir ./settings

# 全カラム情報を含めて出力
dbunit generate -generateType settings \
  -unit dataset \
  -includeAllColumns true \
  -src.srcType table -src.src tables.txt \
  -resultDir ./settings