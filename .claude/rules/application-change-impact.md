# core/application パッケージ変更時の影響確認

`core/src/main/java/yo/dbunitcli/application/` 変更時に必ず実施する。

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

## 3. GUI コードレビュー

`gui/src/main/java/yo/dbunitcli/javafx/view/main/MainPresenter.java` を確認する。Option・ParamType・DTO変更時はフォーム表示を目視確認する。
