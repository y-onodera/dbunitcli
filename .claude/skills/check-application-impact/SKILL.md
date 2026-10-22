---
description: core/application・core/resource変更時のsidecar・tauri影響確認
TRIGGER when: core/src/main/java/yo/dbunitcli/application/ または resource/ 配下のファイル変更を含む実装タスクが完了したとき。
DO NOT TRIGGER when: 上記以外のファイルのみの変更、またはファイルの読み取りのみの場合。
---

以下を順に実施すること。

## 1. sidecar コントローラーテスト実行

ルートで `mvn test -pl sidecar -am -Dtest=<テストクラス> -Dsurefire.failIfNoSpecifiedTests=false`。テストパス: `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/`

| 変更 | テストクラス |
|---|---|
| Compare / Convert / Generate / Run / Parameterize / Scaffold 系 | `<コマンド名>ControllerTest` |
| `FileResources`・パス解決 | `AbstractCommandControllerTest`、`WorkspaceControllerTest` |

共通クラス・基底インターフェース変更時は全テスト対象。失敗時は `sidecar/src/test/resources/yo/dbunitcli/sidecar/controller/` の期待値JSONを更新する。

## 2. tauri フィクスチャ更新

レスポンス構造変更時、`tauri/src/tests/app/form/fixtures.ts` を更新し `CommandForm.test.tsx` / `ConvertForm.test.tsx` が通ることを確認する。

## 3. ヘルプ更新

オプションの意味・パスの解決順・保存内容・テンプレート変数など画面から見える挙動が変わったら、`tauri:update-help` で `tauri/public/help` を更新する。
