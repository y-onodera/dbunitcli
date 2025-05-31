# アプリケーションコンポーネント

このドキュメントでは、DBUnit CLI GUIアプリケーションの機能別コンポーネントについて説明します。

## 1. フォーム管理 (`form/`)

### 1.1 コマンドフォーム
- `CommandForm`: コマンドの種類に応じたフォームを管理
- `CommandFormElement`: 共通のフォーム要素を提供
  - datalist機能により以下のフィールドをサポート:
    - setting: データセット設定ファイル
    - xlsxSchema: Excelスキーマファイル
    - src: SQLクエリファイル（SQLタイプの場合）
    - jdbcProp: JDBC接続設定ファイル
    - template: テンプレートファイル

### 1.2 機能別フォーム
- `CompareForm`: データセット比較機能
- `ConvertForm`: データセット変換機能
- `GenerateForm`: テストデータ生成機能
- `RunForm`: テスト実行機能
- `ParameterizeForm`: パラメータ化機能

## 2. 設定管理 (`settings/`)

### 2.1 データセット設定
- `DatasetSettingsDialog`: データセット設定の一覧表示
- `DatasetSettingDialog`: 個別設定の編集
- `DatasetSettingEditButton`: 設定編集の起動

### 2.2 Excelスキーマ設定
- `XlsxSchemaDialog`: スキーマ設定の一覧表示
- `XlsxCellSettingDialog`: セル単位の設定
- `XlsxRowSettingDialog`: 行単位の設定
- `XlsxSchemaEditButton`: スキーマ編集の起動

### 2.3 SQL編集
- `SqlEditorDialog`: SQL文の編集
- `SqlEditorButton`: エディタの起動
- `ResourceEditButton`: リソース編集の起動

## 3. レイアウト管理 (`main/`)

### 3.1 画面構造
- `Layout`: アプリケーション全体のレイアウトとサイドバー幅の管理
- `Header`: ヘッダー部分の表示
- `Sidebar`: サイドバーの表示と幅のリサイズ機能
- `Form`: メインフォームエリアの管理

## 4. サイドバー管理 (`sidebar/`)

### 4.1 パラメータ管理
- `NamedParameters`: 名前付きパラメータの表示と管理
- `NameEditMenu`: パラメータ名の編集機能
  - 編集状態の内部管理による自己完結的な実装
  - パラメータの編集・コピー・削除機能の提供
  - クリックポジションに応じた適切なメニュー表示

## 5. 結果表示 (`footer/`)

### 5.1 実行結果
- `Footer`: フッター部分の表示
- `ResultDialog`: 実行結果の詳細表示

## 6. 起動処理 (`startup/`)

### 6.1 初期設定
- `StartupForm`: アプリケーション起動時の設定