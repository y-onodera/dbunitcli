---
paths:
  - "src/**/*.ts"
  - "src/**/*.tsx"
---

# このコードベースでの作業方法

## 新しいコマンドの追加

1. `src/app/form/` にフォームコンポーネントを作成
2. `src/model/CommandParam.ts` に対応するモデル型を追加
3. 状態管理用に Context プロバイダを更新
4. メイン `Form.tsx` のルーティングにフォームを追加

## バックエンド統合の変更

- Java プロセス引数は `lib.rs` で設定
- HTTP API 呼び出しは fetch ユーティリティで処理
- ファイルシステム操作は Tauri の dialog プラグインを使用

## 新機能のテスト

- モデルとユーティリティの単体テストを追加
- Context プロバイダの統合テストを追加
- コンポーネントインタラクションテストには Testing Library を使用

## ファイル拡張子の使い分け

### context/ と hooks/ の役割分担
| ディレクトリ | 内容 | 拡張子 |
|---|---|---|
| `context/` | createContext + Provider コンポーネント + context 読み取りフック | `.tsx` |
| `hooks/` | APIコール・状態更新フック（fetchData を使うもの） | `.ts` |
| `utils/` | 汎用ユーティリティ | `.ts` |

### .ts / .tsx の判断フロー
- JSX を返す Provider / Component を定義する → `.tsx`
- `createContext` で context を定義する（context 読み取りフックを同居させる）→ `.tsx`
- テストファイルで JSX（wrapper など）を使う → `.test.tsx`
- APIコール・状態更新のカスタムフック（hooks/ 配置）→ `.ts`
- モデル・ユーティリティ → `.ts`

### hooks/ への追加判断
- `fetchData` を呼ぶカスタムフック → `hooks/*.ts`（APIコールをコンポーネントから分離）
- 複数の context を組み合わせる操作フック → `hooks/*.ts`
