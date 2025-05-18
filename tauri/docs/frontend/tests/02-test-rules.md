# テスト基本規約

このドキュメントは、フロントエンドのテストに関する基本規約を定義します。

## 1. テストファイルの配置

### 1.1. ディレクトリ構造
- テストのディレクトリ構造はsrcのディレクトリ構造と一致させる
  ```
  src/
  ├── model/
  │   └── CommandParam.ts
  └── tests/
      └── model/
          └── CommandParam.test.ts
  ```

### 1.2. 命名規則
- テストファイル名は対象ファイル名に`.test.ts`を付加
- テストケース名は日本語で機能を説明

## 2. テストの基本構造

```typescript
describe('機能カテゴリ', () => {
  it('テストケースの説明', async () => {
    // テストの実装
  });
});
```

## 3. モックの使用方法

### 3.1. APIモック
```typescript
const { mockFetchData } = vi.hoisted(() => ({
  mockFetchData: vi.fn()
}));

vi.mock('../../utils/fetchUtils', () => ({
  fetchData: mockFetchData
}));
```

### 3.2. フィクスチャーの活用
- 共通のモックデータは`setup.ts`に定義
- テストファイル固有のモックは各ファイルで定義

## 4. 非同期処理のテスト

- renderHookとwaitForを組み合わせて使用
- Promiseの解決を適切に待機
- タイムアウトを考慮したテスト設計

## 5. エラーケースのテスト

- 不正な入力値の検証
- APIエラーのハンドリング確認
- バリデーションエラーの確認

詳細な実装例は以下を参照：
- [Contextレイヤーの規約](./03-context-rules.md)
- [Contextテストの実装例](./04-context-examples.md)
- [コンポーネントテスト規約](./05-component-rules.md)