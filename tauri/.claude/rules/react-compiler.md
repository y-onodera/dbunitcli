# React Compiler 前提のコーディング規約

`vite.config.ts` の `reactCompilerPreset()` で有効。`@babel/core` は 7 系固定（8 系は compiler 1.0 非対応）。

- `useCallback` / `useMemo` / `React.memo` は書かない。effect 依存の安定化はモジュールレベル関数で（`hooks-design.md`）
- 依存配列は正確に書く。回避目的の `biome-ignore` 禁止
- コンパイル不可になる書き方禁止（dev/build は黙ってメモ化を諦め、vitest のみ失敗する）
  - フックを値で渡す・props で受けたフックを呼ぶ
  - `props.ref` 参照（分割代入で受ける）
  - コンポーネント内の `catch` 無し `try/finally`・既定値のアロー関数・計算キーの分割代入 → モジュールレベル関数へ
