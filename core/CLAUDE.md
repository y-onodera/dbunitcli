# DBUnit CLI プロジェクト

## プロジェクト概要
DBUnit CLI は、DBUnit フレームワークをベースにした包括的なデータ比較・変換・生成のためのコマンドラインツールです。CSV、Excel、データベース、画像、PDFなど様々なデータ形式に対応し、高度なデータ操作機能を提供します。

## 主要機能

### 1. データ比較 (Compare)
- 複数のデータソース間での行・列レベルの詳細比較
- 差分レポートの生成
- 画像・PDF ファイルの比較機能

### 2. データ変換 (Convert)
- CSV ⇔ Excel ⇔ データベース間のシームレスな変換
- 複数の出力形式への同時変換
- データ型変換と式ベースの値変換

### 3. データ生成 (Generate)
- StringTemplate 4 を使用したテンプレートベースの動的生成
- SQL文、設定ファイル、テキストファイルの生成
- パラメータ化されたバッチ処理

### 4. スクリプト実行 (Run)
- SQL ファイル、ANT スクリプト、バッチファイルの実行
- 外部プロセスとの連携

### 5. パラメータ化実行 (Parameterize)
- 設定ファイルを使用したバッチ処理
- 複数のコマンドの連続実行

## アーキテクチャ構成

### パッケージ構成

#### `yo.dbunitcli.application`
- **Command Classes**: 各コマンドの実装
  - `Compare.java`: データ比較機能
  - `Convert.java`: データ変換機能  
  - `Generate.java`: データ生成機能
  - `Run.java`: スクリプト実行機能
  - `Parameterize.java`: メインエントリーポイント

- **cli/**: Picocli フレームワークを使用したコマンドライン引数解析
- **dto/**: 各コマンド用のデータ転送オブジェクト
- **json/**: JSON 設定ファイルのパーサーとビルダー
- **option/**: コマンド別オプションクラス

#### `yo.dbunitcli.dataset`
- **Core Interfaces**:
  - `ComparableDataSet`: 比較機能付きの拡張 DBUnit IDataSet
  - `ComparableTable`: メタデータ付きテーブル表現
  - `IDataSetConverter`: 形式変換インターフェース

- **compare/**: 差分検出・レポート生成を含む比較エンジン
- **converter/**: フォーマット変換機能 (CSV, Excel, Database)
- **producer/**: 各種入力タイプ用のデータソースプロデューサー

#### `yo.dbunitcli.resource`
- **jdbc/**: データベース接続管理
- **poi/**: Apache POI を使用した Excel ファイル処理
- **st4/**: 動的テキスト生成のための StringTemplate 4 統合

#### `yo.dbunitcli.common`
- **filter/**: データ処理用の各種フィルタ実装

### 対応データソース

#### 入力形式
- **ファイル**: CSV, TSV, Excel (xls/xlsx), 固定長, 正規表現分割テキスト
- **データベース**: JDBC 完全対応（テーブル・SQL クエリ）
- **ディレクトリ**: 複数ファイルの一括処理
- **バイナリ**: 画像・PDF の比較機能

#### 出力形式
- CSV, TSV, Excel (xls/xlsx), データベーステーブル
- テンプレートベースの動的生成 (SQL, テキストファイル等)

### 技術スタック

#### 主要依存関係
- **DBUnit**: データテストの中核フレームワーク
- **Apache POI**: Excel ファイル処理
- **StringTemplate 4**: テンプレートエンジン
- **Picocli**: CLI フレームワーク
- **SLF4J + Logback**: ログ機能
- **JUnit 5 + JMockit**: テストフレームワーク

#### ビルド設定
- **Maven**: ビルド管理
- **GraalVM Native Image**: ネイティブコンパイル対応
- **Java 21**: 最新の Java バージョン対応

## 実行方法

### メインエントリーポイント
```bash
java -jar dbunit-cli-jar-with-dependencies.jar [コマンド] [オプション]
```

### 個別コマンド実行
- `java -cp dbunit-cli.jar yo.dbunitcli.application.Compare [オプション]`
- `java -cp dbunit-cli.jar yo.dbunitcli.application.Convert [オプション]`
- `java -cp dbunit-cli.jar yo.dbunitcli.application.Generate [オプション]`
- `java -cp dbunit-cli.jar yo.dbunitcli.application.Run [オプション]`

### GraalVM ネイティブイメージ
```bash
./dbunit-cli [コマンド] [オプション]
```

## 設定ファイル

### JSON 設定
- テーブル設定、フィルタ設定、スキーマ定義
- 複雑な操作のための包括的な設定オプション
- 設定ファイルのインポート・マージ機能

### テンプレート
- StringTemplate 4 を使用した動的コンテンツ生成
- SQL 生成テンプレート
- パラメータファイルテンプレート
- 設定ファイルテンプレート

## テスト構成

### テストアプローチ
- **統合テスト**: 実データでの完全なエンドツーエンドテスト
- **パラメータ駆動テスト**: パラメータファイルを使用した広範囲なテストシナリオ
- **Expected vs Actual**: 包括的な比較テスト

### テストリソース
- 複数形式での豊富なテストデータ
- 様々なシナリオ用パラメータファイル
- 検証用の期待値ファイル

## 設計パターン

### 1. Command Pattern
各操作が `Command` インターフェースを実装し、一貫した実行フローを提供

### 2. Producer-Consumer Pattern
異なるソースタイプに対応するプロデューサー実装

### 3. Strategy Pattern
- コンバーター、フィルター、データベースオペレーター

### 4. Builder Pattern
複雑なオブジェクトの柔軟な構築

## 主要な設計判断

### 1. 拡張可能アーキテクチャ
プロデューサー・コンバーターパターンにより、コアロジックを変更せずに新しいデータ形式を追加可能

### 2. 設定駆動
JSON 設定ファイルが複雑な操作を制御し、高度なカスタマイズが可能

### 3. テンプレートベース生成
StringTemplate 4 統合により強力な動的コンテンツ生成を実現

### 4. 包括的テスト
パラメータ駆動テストアプローチにより、実世界のシナリオを徹底的にカバー

## 開発・デバッグ情報

### ビルドコマンド
```bash
# 通常のビルド
mvn clean compile

# テスト実行
mvn test

# 統合テスト実行
mvn test -PIntegrationTest

# ネイティブイメージビルド
mvn clean package -Pnative

# 全依存関係を含むJARビルド
mvn clean package
```

### ログ設定
- `src/main/resources/logback.xml` でログレベルを設定
- SLF4J を使用した構造化ログ

### 設定ファイルの場所
- テンプレート: `src/main/resources/settings/`
- テストデータ: `src/test/resources/yo/dbunitcli/application/`

この CLI ツールは、エンタープライズ級のデータ処理ツールとして、優れた関心の分離、包括的なテスト、複数形式にわたる複雑なデータ操作をサポートしています。