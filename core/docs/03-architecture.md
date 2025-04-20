# アーキテクチャと構造

## 1. パッケージ構造

```
yo.dbunitcli
├── Strings.java        # 共通文字列定義
├── application         # アプリケーションのメイン機能
│   ├── cli            # Picocliベースの引数処理
│   ├── dto           # データ転送オブジェクト
│   ├── json          # 設定ファイル処理
│   ├── option        # コマンドオプション
│   ├── Command.java  # 基本コマンド
│   ├── Compare.java  # 比較コマンド
│   ├── Convert.java  # 変換コマンド
│   ├── Generate.java # 生成コマンド
│   ├── Run.java      # 実行コマンド
│   └── Parameterize.java # パラメータ処理コマンド
├── dataset           # データセット操作
│   ├── compare       # データ比較機能
│   ├── converter     # データ変換機能
│   ├── producer      # データセット生成
│   └── ComparableDataSet.java # データセット比較インターフェース
├── fileprocessor     # ファイル処理実行
└── resource         # リソース管理
    ├── jdbc         # データベース接続
    ├── poi          # Excel処理
    └── st4          # テンプレート処理
```

## 2. 主要コンポーネント

### コマンド (application)
- Compare: テーブル単位、行単位のデータ比較
- Convert: CSV/Excel/DB間のデータ変換
- Generate: テンプレートベースのテキスト生成
- Run: SQLの実行とデータ操作
- Parameterize: パラメータ化による一括処理

### データ処理 (dataset)
- データセットの比較：キー指定、カラムフィルタリング
- テーブル操作：結合、分割、マッピング
- データ変換：式による値の変換、型変換
- フィルタリング：行の選択、カラムの追加/除外

### リソースアクセス (resource)
- データベース：JDBCによる接続とSQL実行
- Excel：POIによるxls/xlsx処理
- テンプレート：ST4による動的テキスト生成
- ファイル：CSV/TSV/画像/PDF処理

### 設定管理
- 設定ファイルの読み込みと検証
- データソース設定：入出力形式の制御
- 比較設定：比較ルールのカスタマイズ
- テンプレート設定：生成ルールの定義

## 3. プロジェクト構造

### ソースコード
- `src/main/java/yo/dbunitcli`: Javaソース
- `src/test/java/yo/dbunitcli`: JUnit5/Mockitoテスト

### リソースファイル
- `src/main/resources`: 設定/テンプレート
- `src/main/resources/param`: パラメータ
- `src/main/resources/settings`: 設定テンプレート
- `src/main/resources/sql`: SQLテンプレート

この構造により、高度なカスタマイズ性と拡張性を実現しています。