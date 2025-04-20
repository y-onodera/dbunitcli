# DBUnit CLI ドキュメント

## 目次

### 1. プロジェクト情報
- [プロジェクト概要と技術スタック](02-overview.md)
- [アーキテクチャと構造](03-architecture.md)

### 2. コマンドリファレンス

#### 基本コマンド
- [Compareコマンド](commands/01-compare.md)：データセット間の比較を実行
- [Convertコマンド](commands/02-convert.md)：異なる形式間のデータ変換
- [Generateコマンド](commands/03-generate.md)：SQLやテキストファイルの生成
- [Runコマンド](commands/04-run.md)：SQLやスクリプトの実行
- [Parameterizeコマンド](commands/05-parameterize.md)：パラメータを使用した繰り返し実行

#### テンプレート機能

**基本概念**
- [テンプレート機能の概要](commands/template/01-overview.md)：基本的な使用方法
- [処理単位](commands/template/02-processing-units.md)：record/table/dataset
- [データ構造](commands/template/03-data-structures.md)：パラメータのデータ形式
- [テンプレート構文](commands/template/04-syntax.md)：ST4の文法と機能

**テキスト生成**
- [基本的な例](commands/template/05-basic-examples.md)：SQL/テキスト生成
- [レコード単位処理](commands/template/06-record-examples.md)：1行ごとの処理
- [テーブル単位処理](commands/template/07-table-examples.md)：テーブル単位の処理
- [テーブル単位処理](commands/template/08-dataset-examples.md)：データセット単位の処理

**コマンドでのテンプレート処理**
- [パラメータ処理例](commands/template/09-parameter-examples.md)：Parameterizeコマンド
- [共通利用ガイド](commands/template/10-common-usage.md)：
  - Runコマンドでの動的SQL実行
  - DataSetLoadOptionでのテンプレート使用
  - コマンド間の連携パターン

### 3. オプションリファレンス
- [データソース設定](options/01-data-source.md)：データの読み込み設定
- [出力設定](options/02-result.md)：結果の出力形式
- [データベース設定](options/03-jdbc.md)：JDBC接続設定
- [テンプレート設定](options/04-template.md)：テンプレート処理のオプション
- [画像比較設定](options/05-image.md)：画像比較のオプション

### 4. JSONスキーマ
- [スキーマリファレンス](schemas/01-index.md)：設定ファイルのスキーマ定義

テンプレート機能は大きく2つの用途があります：
1. Generateコマンドでのファイル生成
2. 他のコマンドでの動的SQL生成や値の置換

特にParameterizeコマンドと組み合わせることで、テンプレートベースの柔軟なデータ処理が可能になります。
