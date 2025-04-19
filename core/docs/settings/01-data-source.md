# データソース設定

データの読み込みに関する設定です。様々な形式（CSV、Excel、データベースなど）からのデータ読み込みに対応しています。

## DataSetLoadOption (src.*/new.*/old.*/param.*)

### 基本設定
* -srcType: データソースタイプ - [詳細な設定](#srctype-データソースタイプ)
* -src: データソースパス
* -setting: テーブル設定ファイルのパス
* -settingEncoding: 設定ファイルのエンコーディング

### フィルタリング設定
* -regTableInclude: 含めるテーブルの正規表現パターン
* -regTableExclude: 除外するテーブルの正規表現パターン
* -regInclude: 含めるファイルの正規表現パターン
* -regExclude: 除外するファイルの正規表現パターン

### 制御設定
* -loadData: データを読み込むかどうか（デフォルト: true）
* -includeMetaData: メタデータを含めるかどうか（デフォルト: false）
* -recursive: 再帰的に検索するかどうか（デフォルト: false）

## srcType (データソースタイプ)

### csv: CSVファイル形式
* -delimiter: 区切り文字（デフォルト: カンマ）
* -ignoreQuoted: クォート処理を無視するかどうか
* -encoding: 文字エンコーディング
* -extension: 処理対象とする拡張子
* -headerName: ヘッダー行の名前指定

### xls/xlsx: Excelファイル形式
* -xlsxSchema: Excel形式定義ファイル
* -extension: 処理対象とする拡張子
* -headerName: ヘッダー行の名前指定

### reg: 正規表現による解析
* -regHeaderSplit: ヘッダー行の分割パターン
* -regDataSplit: データ行の分割パターン
* -encoding: 文字エンコーディング
* -extension: 処理対象とする拡張子
* -headerName: ヘッダー行の名前指定

### fixed: 固定長形式
* -fixedLength: カラム長定義
* -encoding: 文字エンコーディング
* -extension: 処理対象とする拡張子
* -headerName: ヘッダー行の名前指定

### table/sql: データベーステーブル/SQL形式
* -useJdbcMetaData: JDBCメタデータを使用するかどうか
* jdbc.*: データベース接続設定
* -encoding: 文字エンコーディング
* -extension: 処理対象とする拡張子
* -headerName: ヘッダー行の名前指定

### その他のタイプ
* dir: ディレクトリ（固有のオプションなし）
* file: ファイル（-extension: 処理対象とする拡張子）
* none: データソースなし