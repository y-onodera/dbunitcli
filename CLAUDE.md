# DBUnit-CLI

Maven マルチモジュール構成の統合アプリケーション。

- `core/` - CLI機能・共通jar・GraalVM native-image対応
- `sidecar/` - Micronaut製REST API（coreをHTTP公開、tauriのバックエンド）
- `gui/` - JavaFX製デスクトップGUI
- `tauri/` - Tauri v2フロントエンド（sidecar APIを利用）
