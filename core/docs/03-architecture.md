# アーキテクチャと構造

## 1. パッケージ構造

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
└── resource        # リソース管理
    ├── jdbc        # データベース接続
    ├── poi         # Excel処理
    └── st4         # テンプレート処理
```

### application
アプリケーションのメイン機能を提供するパッケージです。コマンドライン引数の処理、データ転送オブジェクト、コマンドオプションの管理を行います。

### dataset
データセットの操作に関連する機能を提供します。データの比較、変換、生成などの中核的な機能を実装しています。

### fileprocessor
ファイル処理の実行を担当します。各種ファイルの読み書きや処理を統一的なインターフェースで提供します。

### resource
外部リソースへのアクセスを管理します。データベース接続、Excelファイル処理、テンプレート処理などのリソースアクセスを抽象化します。

## 2. プロジェクト構造

### ソースコード
- `src/main/java/yo/dbunitcli`: Javaのソースファイル
- `src/test/java/yo/dbunitcli`: テストコード

### リソースファイル
- `src/main/resources`: 設定ファイル（logback.xml等）
- `src/main/resources/param`: パラメータテンプレート
- `src/main/resources/settings`: 設定テンプレート
- `src/main/resources/sql`: SQLテンプレート

この構造により、本番コードとテストコード、各種リソースファイルが明確に分離され、保守性の高いプロジェクト構成を実現しています。