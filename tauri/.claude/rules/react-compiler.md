# React Compiler 前提のコーディング規約

`babel-plugin-react-compiler`（vite.config.ts）が有効。コンパイル時に自動メモ化が挿入される。

## 手動メモ化を新規追加しない

`useCallback` / `useMemo` / `React.memo` は**新規で書かない**。
既存コードに残っていても書き換えない（変更スコープを広げない）。

## useEffect 依存配列は正確に書く

依存配列の意味的な正しさは開発者の責任。
省略・回避目的で `biome-ignore` を使わないこと。
