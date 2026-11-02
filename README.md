# dbunitcli

DBUnit CLI はデータセットの変換・比較・生成・SQL 実行・パラメータ化バッチ処理を行う統合ツール群です。

## ドキュメント

- [ヘルプページ（オンラインプレビュー）](https://htmlpreview.github.io/?https://github.com/y-onodera/dbunitcli/blob/master/tauri/public/help/index.html)

ヘルプには以下のトピックを含みます:

### コマンド
- Convert — データ形式を変換する
- Compare — データセットを比較して差分を検出する
- Generate — テンプレートからファイルを生成する
- Run — SQL やスクリプトを実行する
- Parameterize — パラメータでバッチ処理する

### リファレンス
- Dataset Load Form — データセット入力フォームの構成
- Dataset Settings — Dataset Settings ファイルの書式
- Xlsx Schema — Xlsx 入力のマッピング定義

## ログ出力

既定では INFO 以上を、コンソールと実行時のカレントディレクトリの `logs/` に出力します（core は `logs/dbunitcli.log`、sidecar は `logs/dbunit-cli-sidecar.log`）。

TRACE などの詳細なログが必要な場合は、logback 標準のシステムプロパティ `logback.configurationFile` で設定ファイルを指定します。指定したファイルが見つからない場合は既定の設定で動きます。

```
dbunit-cli.exe -Dlogback.configurationFile=C:/work/logback-trace.xml compare ...
```

設定ファイルの例:

```xml
<configuration>
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%t] %-5level %logger - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="TRACE">
        <appender-ref ref="console"/>
    </root>
</configuration>
```

native-image 版で使える設定要素は次のクラスに限られます（他を使う場合は `reachability-metadata.json` への追加が必要）。

- appender: `ConsoleAppender` / `FileAppender` / `RollingFileAppender`
- rollingPolicy: `TimeBasedRollingPolicy`
- encoder: `PatternLayoutEncoder`
- filter: `ThresholdFilter`

## モジュール構成

- `core/` — コア CLI 機能（GraalVM native-image ビルド）
- `sidecar/` — Micronaut ベースの REST API サーバー
- `gui/` — JavaFX GUI
- `tauri/` — Tauri v2 デスクトップアプリ（React フロントエンド）
