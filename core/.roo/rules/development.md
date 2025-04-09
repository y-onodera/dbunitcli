# dbunitcli Rules

## 仕様ドキュメント体系

dbunitcliのコマンドとオプションの仕様はdocフォルダ以下のMarkdownファイルに定義されています：
- .roo-artifacts.md: メインの定義ドキュメント
- .roo-artifacts-commands.md: コマンドの詳細仕様
- .roo-artifacts-settings.md: 共通設定項目の仕様
- .roo-artifacts-json-schema.md: JSONスキーマの定義

## 実装修正時の注意点

1. 修正内容に応じて上記の関連しそうなドキュメントを確認すること
2. applicationパッケージ以下の実装を修正した際は、仕様ドキュメントの更新要否を確認すること
3. 仕様と実装の整合性を維持すること

## 技術スタック

### ビルドツール
- maven

### CLIライブラリ
- picocli

### テストフレームワーク
- JUnit5
- Mockito

### ランタイム
- graalvm (native image生成用)

## 開発規約

### コード品質
- 実装コードを省略せず、完全な形で提供すること
- セキュリティのベストプラクティスに従った実装を行うこと
- コメントや説明は日本語で詳細に記述すること

### セキュリティ要件

#### 機密情報の取り扱い
- APIキー、トークン、認証情報を含むファイルの読み取りと変更を禁止
- 機密ファイルを絶対にコミットしない
- シークレット情報は環境変数を使用する
- ログ出力に認証情報を含めない

## アーキテクチャ設計

### パッケージ構造

```
yo.dbunitcli
├── application      # アプリケーションのメイン機能
│   ├── cli         # CLIの引数処理
│   ├── dto         # データ転送オブジェクト
│   └── option      # コマンドオプション
├── dataset         # データセット操作
│   ├── compare     # データ比較機能
│   ├── converter   # データ変換機能
│   └── producer    # データセット生成
├── fileprocessor   # ファイル処理実行
└── resource       # リソース管理
    ├── jdbc       # データベース接続
    ├── poi        # Excel処理
    └── st4        # テンプレート処理
```

### ディレクトリ構造

#### ソースコード
- Javaのファイルは `main/java/yo/dbunitcli` に配置
  - `application`: アプリケーションのコマンドやオプション関連クラス
  - `dataset`: データセットの操作関連クラス
  - `fileprocessor`: ファイル処理関連クラス
  - `resource`: リソース管理関連クラス

#### リソースファイル
- `main/resources` 配下に配置
  - 設定ファイル（logback.xml等）: `main/resources`直下
  - パラメータテンプレート: `param`ディレクトリ
  - 設定テンプレート: `settings`ディレクトリ
  - SQLテンプレート: `sql`ディレクトリ

#### テストコード
- `test/java/yo/dbunitcli` に配置
  - `application`: アプリケーションのテストクラス
  - `dataset`: データセットのテストクラス
  - `resource`: リソース管理のテストクラス