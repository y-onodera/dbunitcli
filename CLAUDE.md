# DBUnit-CLI

Maven マルチモジュール構成の統合アプリケーション。

- `core/` - CLI機能・共通jar・GraalVM native-image対応
- `sidecar/` - Micronaut製REST API（coreをHTTP公開、tauriのバックエンド）
- `gui/` - JavaFX製デスクトップGUI
- `tauri/` - Tauri v2フロントエンド（sidecar APIを利用）

## Claude Code on the web 用セットアップ
`.claude/settings.json` の SessionStart フックが `.claude/start-maven-proxy.sh` を実行し、JDK 25 導入・`~/.mavenrc` 作成・（認証付きプロキシ環境なら）`.claude/maven-proxy.py` の起動を行う。ローカル（Windows）では使わない。web 環境のプロキシ方式やJDK配布URLが変わって Maven/Java が動かなくなったら、このスクリプトをメンテする。
Stop フック（`.claude/hooks/stop-tauri-test.sh`）は `tauri/` に未テストの変更があるときだけ vitest を実行し、失敗したら停止を止める（ローカル・web 共通）。
