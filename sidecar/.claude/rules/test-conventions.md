# コントローラーテスト方針

## テスト用ワークスペース
- ソース `src/test/resources/workspace/sample/` を `process-test-resources` で `target/test-temp/workspace/sample/` へコピー（ソースツリーを汚さない）
- テストクラスは `@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")` で指定
- ファイルを作るテストは `@AfterEach` で生成物（`option/parameterize/*.txt`、`option/parameterize/template/*`、ワークスペース直下の `*.csv` など）を削除し冪等に保つ

## レスポンス検証
- 期待値JSONは `src/test/resources/yo/dbunitcli/sidecar/controller/` に置き、`JsonTestHelper.assertJsonEquals`（空白を除いて全体比較）で検証
- ファイル作成を伴うテストは `Files.exists()` で副作用も検証

## コード変更時に更新するもの
| 変更 | 更新対象 |
|---|---|
| `AbstractCommandController` / `AbstractResourceFileController` | 継承する全コントローラーのテスト |
| レスポンスJSON構造 | 対応する期待値JSON |
| リクエスト/レスポンスDTO | そのDTOを使うテストのリクエストJSON |
| `@Controller` のパス | テスト内のリクエストURL |
