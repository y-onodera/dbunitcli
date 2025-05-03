# Convertコマンド

## 基本機能
データセットの形式を別の形式に変換します。例えば、CSVからExcel、データベースからCSVなど、様々な形式間の変換に対応しています。

## 引数
* src.* : 変換元データセットの設定 - [データソース設定](../options/02-data-source.md)
* result.* : 変換結果の出力設定 - [出力設定](../options/03-result.md)
* -parameter : パラメータ設定

## 使用例
```bash
# CSVファイルをExcelに変換
dbunit convert \
  -src.srcType csv -src.src input.csv \
  -result.resultType xlsx -result.resultPath output.xlsx

# データベースのテーブルをCSVに出力
dbunit convert \
  -src.srcType table -src.src "SELECT * FROM users" \
  -jdbc.url "jdbc:mysql://localhost/testdb" \
  -jdbc.user "root" -jdbc.pass "password" \
  -result.resultType csv -result.resultDir ./output
```

## 変換可能なフォーマット
### 入力フォーマット
- CSV/TSV
- Excel(xls/xlsx)
- データベーステーブル
- SQL検索結果
- 固定長テキスト
- 正規表現解析テキスト

### 出力フォーマット
- CSV
- Excel(xlsx)
- データベーステーブル
- JSON
- XML

## 注意事項
- データ型は自動的に変換されますが、一部の特殊な型は明示的な変換設定が必要です
- 大容量データを扱う場合は、メモリ使用量に注意が必要です
- データベース接続を使用する場合は、適切な権限が必要です