# Contextレイヤーのテスト規約

このドキュメントは、アプリケーションのContextレイヤーにおけるテスト実装規約を定義します。

## 1. テスト環境のセットアップ

### 1.1. モックデータの定義

| オブジェクト | モックデータの目的 |
|-------------|------------------|
| mockResponse | APIレスポンスの定義 |
| mockEnviroment | 環境設定の定義 |
| mockWorkspaceResources | ワークスペース情報の定義 |

- 型定義を明示的に指定
- フィクスチャーを活用して共通データを定義
- 実データに基づいた最小限のモックを作成

### 1.2. コンテキストのラッピング

テスト対象のコンテキストをラップするためのwrapperコンポーネントを定義します。

```typescript
function MockProvider({ children }: { children: React.ReactNode }) {
  return (
    <enviromentContext.Provider value={mockEnviroment}>
      <TargetProvider>{children}</TargetProvider>
    </enviromentContext.Provider>
  );
}
```

## 2. テストケースの実装

### 2.1. 初期状態のテスト

- デフォルト値の確認
- 初期化プロセスの完了確認
- 非同期初期化の待機処理

### 2.2. データ操作のテスト

以下の操作について、正常系と異常系をテスト：

1. データのロード
2. データの保存
3. データの削除
4. データの更新
5. データのリフレッシュ

### 2.3. エラーケースのテスト

- APIエラーのハンドリング
- バリデーションエラーの処理
- 通信タイムアウトの処理

## 3. 実装のポイント

- renderHookとwaitForを組み合わせて非同期処理をテスト
- actを使用して状態更新をラップ
- テストケース名は日本語で機能を説明
- エンドポイントごとにモックレスポンスを定義

具体的な実装例は[04-context-examples.md](./04-context-examples.md)を参照してください。