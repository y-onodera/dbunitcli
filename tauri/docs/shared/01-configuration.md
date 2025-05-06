# 共有設定

このドキュメントでは、フロントエンドとバックエンドで共有している設定について説明します。

## 1. アプリケーション設定 (tauri.conf.json)

### 1.1 ビルド設定
```json
{
  "build": {
    "beforeDevCommand": "bun run dev",
    "beforeBuildCommand": "bun run build",
    "frontendDist": "../dist",
    "devUrl": "http://localhost:1420"
  }
}
```
- 開発サーバーの設定
- ビルドコマンドの定義
- フロントエンドの出力先

### 1.2 バンドル設定
```json
{
  "bundle": {
    "resources": [
      "backend/dbunit-cli-sidecar.exe",
      "backend/*.dll",
      "backend/lib/*"
    ]
  }
}
```
- バンドルに含めるリソース
- Javaバイナリの指定
- 依存ライブラリの指定

### 1.3 コマンドライン引数
```json
{
  "plugins": {
    "cli": {
      "args": [
        {"name": "port", "takesValue": true},
        {"name": "workspace", "takesValue": true},
        {"name": "dataset.base", "takesValue": true},
        {"name": "result.base", "takesValue": true}
      ]
    }
  }
}
```

## 2. 開発環境設定 (vite.config.ts)

### 2.1 Tauri連携
```typescript
{
  clearScreen: false,
  server: {
    port: 1420,
    strictPort: true,
    watch: {
      ignored: ["**/src-tauri/**"]
    }
  }
}
```
- 開発サーバーの設定
- ポート番号の固定
- ウォッチ対象の制御

### 2.2 テスト設定
```typescript
{
  test: {
    environment: "jsdom",
    setupFiles: ["./src/tests/setup.ts"],
    include: ["src/**/*.{test,spec}.{js,ts,jsx,tsx}"]
  }
}
```

## 3. セキュリティ設定 (capabilities/default.json)

### 3.1 権限定義
```json
{
  "permissions": [
    "core:default",
    "shell:allow-open",
    "dialog:allow-open",
    "dialog:allow-save",
    "cli:allow-cli-matches"
  ]
}
```
- 必要最小限の権限設定
- シェル実行の制限
- ファイル操作の制限

### 3.2 HTTP通信
```json
{
  "http:default": {
    "allow": [
      {"url": "http://localhost:*/"}
    ]
  }
}
```
- ローカルホストとの通信許可
- ポート制限の設定