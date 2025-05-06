# コマンドライン引数

このドキュメントでは、DBUnit CLI GUIアプリケーションのコマンドライン引数処理について説明します。

## 1. 引数定義

### 1.1 基本設定
- port: サーバーポート番号
- workspace: ワークスペースパス
- dataset.base: データセットベースパス
- result.base: 結果出力ベースパス

### 1.2 デフォルト値
```rust
add_arg(&matches, &mut args, "port", "-Dmicronaut.server.port=", "8080");
add_arg(&matches, &mut args, "workspace", "-Dyo.dbunit.cli.workspace=", ".");
add_arg(&matches, &mut args, "dataset.base", "-Dyo.dbunit.cli.dataset.base=", ".");
add_arg(&matches, &mut args, "result.base", "-Dyo.dbunit.cli.result.base=", ".");
```

## 2. 引数処理

### 2.1 引数解析
```rust
match app.cli().matches() {
    Ok(matches) => {
        // 引数処理
    }
    Err(e) => println!("{:?}", e),
}
```
- Tauriプラグインによる引数解析
- エラー処理の実装
- マッチングパターン

### 2.2 引数変換
```rust
fn add_arg(
    matches: &tauri_plugin_cli::Matches,
    args: &mut Vec<String>,
    key: &str,
    arg: &str,
    default_value: &str,
)
```
- 引数の取得と変換
- デフォルト値の設定
- システムプロパティ形式への変換

## 3. 環境変数連携

### 3.1 JAVA_TOOL_OPTIONS
```rust
if let Ok(java_tool_options) = env::var("JAVA_TOOL_OPTIONS") {
    java_tool_options_args
        .extend(java_tool_options.split_whitespace().map(String::from));
}
```
- 環境変数からの設定読み込み
- 引数への追加
- 空白区切りの解析

### 3.2 引数の結合
```rust
args.extend(java_tool_options_args);
```
- コマンドライン引数と環境変数の統合
- 優先順位の管理
- フォーマットの統一

## 4. エラー処理

### 4.1 バリデーション
- 必須引数のチェック
- 値の妥当性検証
- 型変換エラーの処理

### 4.2 エラー通知
- エラーメッセージの生成
- ログ出力
- ユーザーへのフィードバック