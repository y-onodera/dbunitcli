---
description: core/application変更時のsidecar・tauri影響確認
TRIGGER when: core/src/main/java/yo/dbunitcli/application/ 配下のファイル変更を含む実装タスクが完了したとき。
DO NOT TRIGGER when: core/application以外のファイルのみの変更、またはファイルの読み取りのみの場合。
---

以下を順に実施すること。

## 1. sidecar コントローラーテスト実行

テストパス: `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/`

| 変更 | テストクラス |
|---|---|
| Compare系 | `CompareControllerTest` |
| Convert系 | `ConvertControllerTest` |
| Generate系 | `GenerateControllerTest` |
| Run系 | `RunControllerTest` |
| Parameterize系 | `ParameterizeControllerTest` |

共通クラス・基底インターフェース変更時は全テスト対象。失敗時は `sidecar/src/test/resources/yo/dbunitcli/sidecar/controller/` の期待値JSONを更新する。

## 2. tauri フィクスチャ更新

レスポンス構造変更時、`tauri/src/tests/app/form/fixtures.ts` を更新し `CommandForm.test.tsx` / `ConvertForm.test.tsx` が通ることを確認する。
