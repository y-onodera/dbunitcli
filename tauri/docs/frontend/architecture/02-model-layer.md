# モデルレイヤー

このドキュメントでは、DBUnit CLI GUIアプリケーションのモデルレイヤーの構成を説明します。

## 1. ワークスペース管理 (`WorkspaceResources.ts`)

### 1.1 主要なデータ構造
- `WorkspaceResources`: アプリケーション全体のリソース構成
  - `context`: ワークスペースの基本設定
  - `parameterList`: コマンドパラメータの一覧
  - `resources`: 各種リソースファイルの設定

### 1.2 パラメータ管理
- `ParameterList`: 各種コマンドのパラメータを管理
  - パラメータの作成、更新、削除機能を提供
  - イミュータブルな操作を実装

## 2. データセット設定 (`DatasetSettings.ts`)

### 2.1 設定管理
- `DatasetSettings`: データセット設定のルートクラス
  - 設定の追加、更新、削除機能を提供
  - 共通設定と個別設定の管理

### 2.2 詳細設定
- `DatasetSetting`: 個々の設定を管理
  - テーブル結合設定
  - フィルター条件
  - データ変換ルール

## 3. Excelスキーマ管理 (`XlsxSchema.ts`)

### 3.1 スキーマ定義
- `XlsxSchema`: Excelファイルの構造定義
  - 行ベースの設定
  - セルベースの設定
  - スキーマの永続化機能

### 3.2 詳細設定
- `RowSetting`: 行ベースのデータ抽出設定
- `CellSetting`: セルベースのデータ抽出設定

## 4. クエリデータソース管理 (`QueryDatasource.ts`)

### 4.1 データソース定義
- `QueryDatasource`: SQLクエリ実行用のデータソース
  - データソースの種類（csvq, sql, table）
  - ファイル名と内容の管理

## 特徴

1. イミュータブル性
   - すべてのモデルクラスは状態の変更を新しいインスタンスとして返す
   - 副作用のない純粋な操作を提供

2. ビルダーパターン
   - 各モデルはビルダーインターフェースを提供
   - 柔軟な初期化と構築を可能に

3. 型安全性
   - TypeScriptの型システムを活用
   - 実行時エラーを防止