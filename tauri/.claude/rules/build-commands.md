# ビルドコマンド

## フロントエンド

```bash
bun install       # 依存関係インストール
bun run dev       # Tailwind CSS コンパイル + Vite 開発サーバー起動
bun run build     # Tailwind CSS コンパイル + TypeScript チェック + Vite ビルド + テスト実行
```

## Tauri

```bash
bun tauri dev     # 開発モード（bun run dev + Rust ビルド + アプリ起動）
bun tauri build   # 配布ビルド（bun run build + Rust リリースビルド）
```

## テスト

```bash
bun vitest run                                             # 全テスト実行
bun vitest run src/tests/model/CommandParam.test.ts        # 特定ファイル実行
```

## コード品質（Biome v2）

```bash
bunx biome format --write .   # フォーマット（全ファイル）
bunx biome lint .              # リント確認
bunx biome lint --write .      # リント自動修正（safe fix）
bunx tsc --noEmit              # TypeScript 型チェック
```

VSCode 保存時: フォーマット + import 整理は自動実行（`biome.json` + `.vscode/settings.json`）
