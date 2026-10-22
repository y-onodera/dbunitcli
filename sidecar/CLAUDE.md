# DBUnit CLI Sidecar

DBUnit CLI のREST APIサーバー。Micronaut ベースで、Tauri アプリケーションのバックエンドとして機能。

## 主要機能
- **コマンドAPI**: core の各コマンドをHTTP APIとして提供（パラメータの追加・読込・保存・実行・.bat出力など）
- **ワークスペース管理**: ディレクトリベースの作業環境管理・コンテキスト切り替え
- **リソース管理**: JDBC設定・テンプレート・Excelスキーマ・データセット設定・固定長カラム定義の CRUD

## パッケージ構成（`yo.dbunitcli.sidecar`）
- `controller/` — `AbstractCommandController`（コマンド系の共通基底）/ `AbstractResourceFileController`（リソース系の共通基底）と各コントローラー。パスは各クラスの `@Controller`
- `domain/project/` — `Workspace`（ワークスペースとシステムプロパティの管理）ほか
- `dto/` — リクエスト/レスポンス DTO

コンテキストパス: `/dbunit-cli`

### ジョブ（`@Interruptible` を付けたメソッドを `Accept: text/event-stream` で呼ぶ）
- 長時間化しうる、かつ割り込んでも安全なメソッド（読み取りのみ・出力を作り直せる）に付ける。save等の書き込み系には付けない
- `job.JobStreamFilter` が1本のSSEで返す: `event: log`（`X-Job-Log` 付きのみ）→ `event: result`（data: `{"status","reason","body"}`。エラー応答も同じ）。1秒ごとにハートビート。他のAcceptは通常の応答
- 中断はクライアントの切断（ハートビートの書き込みで検知し、実行スレッドへinterrupt）
- filterは応答を返す前に `chain.proceed` を購読する（後だとControllerが動かない）。Micronaut版上げ時は `JobStreamTest` で確認

## 技術スタック
- Micronaut 4.x + Netty + Serde Jackson
- Java 25、GraalVM Native Image 対応

## システムプロパティ（定数は `FileResources.PROPERTY_*`）
- `yo.dbunit.cli.workspace`: ワークスペースディレクトリパス
- `yo.dbunit.cli.dataset.base`: データセットベースディレクトリ
- `yo.dbunit.cli.result.base`: 結果出力ベースディレクトリ
- `yo.dbunit.cli.env`: 環境変数ファイル（workspaceからの相対パス。既定 `env.properties`）

## テスト
ルートで `mvn test -pl sidecar -am`（`-am` が無いと親pom・coreを解決できず失敗する）。テスト方針は `.claude/rules/test-conventions.md`
