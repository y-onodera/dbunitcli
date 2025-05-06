# データフロー

このドキュメントでは、DBUnit CLI GUIアプリケーションの主要なデータフローを説明します。

## 1. アプリケーション初期化フロー

### 1.1 環境設定の初期化
1. `EnviromentProvider`がTauriプラグインから環境情報を取得
2. APIエンドポイントURLとベースパスを設定
3. 他のコンテキストで利用可能な状態に

### 1.2 ワークスペースの初期化
1. `WorkspaceResourcesProvider`が`WorkspaceResources`モデルを初期化
2. APIからリソース情報を取得
3. 各種設定とパラメータリストを状態として保持

## 2. データセット設定フロー

### 2.1 設定の読み込み
1. `DatasetSettingsProvider`がAPI経由で設定を取得
2. 取得したJSONを`DatasetSettings`モデルにマッピング
3. モデルの状態をUIに反映

### 2.2 設定の保存
1. UIからの入力を`DatasetSettings`モデルに変換
2. `DatasetSettingsProvider`がAPIを通じて保存
3. `WorkspaceResources`の設定一覧を更新

## 3. Excelスキーマ管理フロー

### 3.1 スキーマの読み込み
1. `XlsxSchemaProvider`がAPI経由でスキーマを取得
2. 取得したJSONを`XlsxSchema`モデルにマッピング
3. モデルの状態をUIに反映

### 3.2 スキーマの保存
1. UIからの入力を`XlsxSchema`モデルに変換
2. `XlsxSchemaProvider`がAPIを通じて保存
3. `WorkspaceResources`のスキーマ一覧を更新

## 4. パラメータ操作フロー

### 4.1 パラメータの選択
1. UIでパラメータを選択
2. `SelectParameterProvider`が該当パラメータをロード
3. パラメータの種別に応じたフォームを表示

### 4.2 パラメータの実行
1. フォームから実行パラメータを収集
2. `SelectParameterProvider`がAPIを通じて実行
3. 実行結果をUIに反映

## 5. エラー処理フロー

### 5.1 API通信エラー
1. `fetchUtils`で統一的なエラーハンドリング
2. エラー情報をUIに反映
3. 適切なフォールバック処理を実行

### 5.2 バリデーションエラー
1. モデルレイヤーでデータ検証
2. エラー情報をコンテキストで管理
3. UIでエラーメッセージを表示