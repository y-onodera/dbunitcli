# プロセス管理

このドキュメントでは、DBUnit CLI GUIアプリケーションのJavaプロセス管理について説明します。

## 1. プロセス起動

### 1.1 Javaプロセスの初期化
```rust
Command::new("backend/dbunit-cli-sidecar.exe")
    .creation_flags(0x08000000) // CREATE_NO_WINDOW
    .args(args)
    .stdout(Stdio::piped())
    .stderr(Stdio::piped())
    .spawn()
```
- ウィンドウレスモードで起動
- 標準出力・エラー出力のパイプ処理
- コマンドライン引数の受け渡し

### 1.2 Java環境設定
```rust
let mut args = vec![String::from("-Djava.home=backend")];
```
- Java実行環境の設定
- システムプロパティの設定
- 環境変数の反映

## 2. プロセス監視

### 2.1 チャネル通信
```rust
let (tx, rx) = sync_channel::<i64>(1);
```
- メインプロセスとの通信
- 同期チャネルの使用
- プロセス終了制御

### 2.2 監視スレッド
```rust
thread::spawn(move || loop {
    let s = rx.recv();
    if s.unwrap() == -1 {
        child.kill().expect("Failed to stop child process");
    }
});
```
- バックグラウンド監視
- 終了シグナルの受信
- プロセス強制終了

## 3. 終了処理

### 3.1 正常終了
```rust
WindowEvent::Destroyed => {
    tx.send(-1).expect("Failed to stop child process");
}
```
- アプリケーション終了時の処理
- リソースのクリーンアップ
- 終了シグナルの送信

### 3.2 エラー処理
- プロセス起動失敗時の処理
- 実行時エラーのハンドリング
- リソースの解放

## 4. リソース管理

### 4.1 標準出力制御
- 出力のパイプ処理
- バッファ管理
- ログ出力

### 4.2 プロセス分離
- セキュリティ境界の確保
- リソースの独立性
- クリーンな終了処理