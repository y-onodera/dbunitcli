# DBUnit CLI ドキュメント

## 目次

### 1. プロジェクト情報
- [プロジェクト概要と技術スタック](02-overview.md)
- [アーキテクチャと構造](03-architecture.md)

### 2. コマンドリファレンス

#### 基本コマンド
- [Compareコマンド](commands/02-compare.md)：データセット間の比較を実行
- [Convertコマンド](commands/03-convert.md)：異なる形式間のデータ変換
- [Generateコマンド](commands/04-generate.md)：SQLやテキストファイルの生成
- [Runコマンド](commands/05-run.md)：SQLやスクリプトの実行
- [Parameterizeコマンド](commands/06-parameterize.md)：パラメータを使用した繰り返し実行

#### テンプレート機能

**基本概念**
- [テンプレート機能の概要](options/template/01-overview.md)：基本的な使用方法
- [処理単位](options/template/02-processing-units.md)：record/table/dataset
- [データ構造](options/template/03-data-structures.md)：パラメータのデータ形式
- [テンプレート構文](options/template/04-syntax.md)：ST4の文法と機能

**コマンドでのテンプレート処理**
- [共通利用ガイド](options/template/05-common-usage.md)：
  - DataSetLoadOptionでのテンプレート使用
  - コマンド間の連携パターン
- [パラメータ処理例](options/template/06-parameter-examples.md)：Parameterizeコマンド

### 3. オプションリファレンス
- [オプション設定の概要](options/01-index.md)：オプション設定の全体像
- [データソース設定](options/02-data-source.md)：データの読み込み設定
- [出力設定](options/03-result.md)：結果の出力形式
- [データベース設定](options/04-jdbc.md)：JDBC接続設定
- [テンプレート設定](options/05-template.md)：テンプレート処理のオプション
- [画像比較設定](options/06-image.md)：画像比較のオプション

### 4. JSONスキーマ
- [スキーマリファレンス](json/01-index.md)：設定ファイルのスキーマ定義
