# コンテキストレイヤー

このドキュメントでは、DBUnit CLI GUIアプリケーションのコンテキストレイヤーの構成を説明します。

## 1. 基盤コンテキスト (`EnviromentProvider.tsx`)

### 1.1 環境設定管理
- Tauriプラグインと連携して環境情報を取得
- アプリケーション全体で利用される設定を提供：
  - APIエンドポイントURL
  - ワークスペースパス
  - データセットベースパス
  - 結果出力ベースパス

## 2. ワークスペース管理 (`WorkspaceResourcesProvider.tsx`)

### 2.1 状態管理
- `WorkspaceResources`モデルの状態を管理
- パラメータリストの更新機能を提供
- リソース設定の一元管理

### 2.2 操作機能
- パラメータの追加/削除
- パラメータ名の変更
- ワークスペース設定の更新

## 3. データセット設定管理 (`DatasetSettingsProvider.tsx`)

### 3.1 設定操作
- `DatasetSettings`モデルのCRUD操作を提供
- 設定の非同期ロード
- 設定の永続化

### 3.2 API連携
- 設定のロード/保存/削除
- エラーハンドリング
- WorkspaceResourcesProviderと連携

## 4. Excelスキーマ管理 (`XlsxSchemaProvider.tsx`)

### 4.1 スキーマ操作
- `XlsxSchema`モデルのCRUD操作を提供
- スキーマの非同期ロード
- スキーマの永続化

### 4.2 API連携
- スキーマのロード/保存/削除
- エラーハンドリング
- WorkspaceResourcesProviderと連携

## 5. パラメータ選択管理 (`SelectParameterProvider.tsx`)

### 5.1 選択状態管理
- 現在選択中のパラメータを管理
- パラメータの実行制御
- パラメータの保存機能

### 5.2 操作機能
- パラメータのリフレッシュ
- 実行結果のハンドリング
- API通信の制御

## 特徴

1. コンテキスト間の依存関係
   - EnviromentProviderを基盤として利用
   - WorkspaceResourcesProviderとの連携
   - シンプルな状態管理はコンポーネント内で実装

2. モデルとの関係
   - モデルの状態管理
   - モデルの操作をラップ
   - 永続化を担当

3. API通信パターン
   - fetchUtilsを使用した統一的な通信
   - エラーハンドリングの一元化
   - 非同期操作の管理