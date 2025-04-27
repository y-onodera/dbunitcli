# 出力設定

処理結果の出力形式や出力先の制御に関する設定です。CSV、Excel、データベースなど、様々な形式での出力に対応しています。

## ResultOption (result.*)

### 基本設定
* -resultType: 結果出力タイプ - [詳細な設定](#resulttype-結果出力形式)
* -resultDir: 結果出力ディレクトリ
* -outputEncoding: 出力エンコーディング

## resultType (結果出力形式)

### csv: CSV形式で出力
* -outputEncoding: 出力エンコーディング
* -exportHeader: ヘッダー行を出力するかどうか
* -exportEmptyTable: 空テーブルを出力するかどうか
* 出力ファイル: [テーブル名].csv

### xls/xlsx: Excel形式で出力
* -outputEncoding: 出力エンコーディング
* -exportHeader: ヘッダー行を出力するかどうか
* -exportEmptyTable: 空テーブルを出力するかどうか
* -excelTable: テーブルの出力形式
  - SHEET: 1ブックに全テーブルを出力（シート分割）
  - BOOK: テーブルごとにブックを作成

#### Excelの自動フォーマット
* 数値: スケールに応じて自動的にフォーマット設定
* 日付: yyyy-MM-dd HH:mm:ss形式
* フォント: MSゴシック、8ポイント

### table: DBを更新
* -operation: 更新方法
  - INSERT: INSERT
  - DELETE: DELETE
  - UPDATE: UPDATE
  - CLEAN_INSERT: TRUNCATE+INSERT
  - DELETE_INSERT: DELETE+INSERT
* -commit: コミット有無（デフォルト: true）
* jdbc.*: [データベース接続設定](04-jdbc.md#jdbc-データベース接続設定)

## エンコーディング設定
* -encoding: ファイルの文字エンコーディング
  - 未指定の場合はシステムのデフォルトエンコーディングを使用