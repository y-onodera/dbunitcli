# コントローラーテスト方針

## テスト用ワークスペース

- ソース: `src/test/resources/workspace/sample/`
- コピー先: `target/test-temp/workspace/sample/`（`maven-resources-plugin` が `process-test-resources` フェーズで自動コピー）
- テストクラスは `@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")` で指定
- `target/` 配下を使うことでソースツリーを汚染しない

## ファイルを作成するテスト（parameterize など）

- テストで作成されるファイルは `target/test-temp/workspace/sample/` 配下に生成される
- `@AfterEach tearDown` で生成ファイルを削除してテストを冪等に保つ
- 削除対象は `option/parameterize/*.txt`、`option/parameterize/template/*`、ワークスペース直下の `*.csv` など

## レスポンス検証

- 期待値 JSON を `src/test/resources/yo/dbunitcli/sidecar/controller/` に格納
- `normalizeJson`（空白・改行除去）で正規化してから `assertEquals` で全フィールドを比較
- ファイル作成を伴うテストは `Files.exists()` で副作用も検証

## コード変更時のcontrollerテスト更新ルール

### 基底クラスを変更したとき
`AbstractCommandController` または `AbstractResourceFileController` を変更したら、
それを継承する全コントローラーのテストが影響を受ける前提で確認する。

### レスポンス構造を変更したとき
コントローラーのレスポンスJSON構造が変わったら（フィールド追加・削除・型変更を含む）、
`src/test/resources/yo/dbunitcli/sidecar/controller/` 配下の対応する期待値JSONファイルを更新する。

### DTOのフィールドを変更したとき
リクエスト/レスポンス用DTOのフィールドを変更したら、そのDTOを使うコントローラーテストを確認する。
リクエストボディの形式が変わるとデシリアライズが失敗するため、テスト内のリクエストJSONも合わせて更新する。

### `@Controller` のパスを変更したとき
コントローラークラスの `@Controller` アノテーションのパスを変更したら、
テスト内のHTTPリクエストURLを合わせて更新する。
