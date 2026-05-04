---
paths:
  - "src/**/*.ts"
  - "src/**/*.tsx"
---

# 作業方法

## 新コマンド追加
1. `src/app/form/` にフォームコンポーネントを作成
2. `src/model/CommandParam.ts` にモデル型を追加
3. Context プロバイダを更新
4. `Form.tsx` のルーティングに追加

## バックエンド統合
- Java プロセス引数は `lib.rs` で設定
- HTTP API → fetch ユーティリティ、FS操作 → Tauri dialog プラグイン

## テスト・ファイル拡張子
テスト: モデル・ユーティリティ → 単体、Context → 統合、コンポーネント → Testing Library


| ディレクトリ | 内容 | 拡張子 |
|---|---|---|
| `context/` | createContext + Provider + contextフック | `.tsx` |
| `hooks/` | fetchData・状態更新フック | `.ts` |
| `utils/` | 汎用ユーティリティ | `.ts` |

- JSX を返す / `createContext` → `.tsx`、テストで JSX → `.test.tsx`
- hooks/ 追加条件: fetchData を呼ぶ、または複数 context を組み合わせるフック
