---
description: tauri フロントエンドのビルド・テストコマンドリファレンス。
---

# ビルドコマンド

## フロントエンド

```powershell
bun install       # 依存関係インストール
bun run dev       # Tailwind CSS コンパイル + Vite 開発サーバー起動
bun run build     # Tailwind CSS コンパイル + TypeScript チェック + Vite ビルド + テスト実行
```

## Tauri

```powershell
bun tauri dev     # 開発モード（bun run dev + Rust ビルド + アプリ起動）
bun tauri build   # 配布ビルド（bun run build + Rust リリースビルド）
```

## テスト

```powershell
bun vitest run                                             # 全テスト実行
bun vitest run src/tests/model/CommandParam.test.ts        # 特定ファイル実行
```

## コード品質（Biome v2）

```powershell
bunx biome format --write .   # フォーマット（全ファイル）
bunx biome lint .              # リント確認
bunx biome lint --write .      # リント自動修正（safe fix）
bunx tsc --noEmit              # TypeScript 型チェック
```
