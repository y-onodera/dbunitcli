# coreアプリケーションパッケージ変更時の影響確認

`core/src/main/java/yo/dbunitcli/application/` 配下を変更したとき、以下の手順で影響範囲を確認すること。

## 対象となる変更

- `command/` 配下のコマンドクラス（`Compare.java`, `Convert.java`, `Generate.java`, `Run.java`, `Parameterize.java`）
- `command/` 配下のDTOクラス（`CompareDto.java`, `ConvertDto.java` 等）
- `command/` 配下のOptionクラス（`CompareOption.java`, `ConvertOption.java` 等）
- `option/` 配下の共通オプションクラス（`DataSetLoadOption.java` 等）
- `Option.java`, `Command.java`, `CommandDto.java`, `CommandLineOption.java` 等の基底クラス・インターフェース

## 1. sidecar: JUnitコントローラーテストで影響を確認する

変更したクラスに対応するコントローラーテストを実行し、レスポンスが壊れていないことを確認する。

### テストファイルの対応表

| 変更クラス（command/） | テストファイル |
|---|---|
| `Compare` / `CompareDto` / `CompareOption` | `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/CompareControllerTest.java` |
| `Convert` / `ConvertDto` / `ConvertOption` | `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/ConvertControllerTest.java` |
| `Generate` / `GenerateDto` / `GenerateOption` | `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/GenerateControllerTest.java` |
| `Run` / `RunDto` / `RunOption` | `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/RunControllerTest.java` |
| `Parameterize` / `ParameterizeDto` / `ParameterizeOption` | `sidecar/src/test/java/yo/dbunitcli/sidecar/controller/ParameterizeControllerTest.java` |

- `option/` 配下の共通クラスや基底クラス・インターフェースを変更した場合は、上記すべてのコントローラーテストを確認する
- テストが失敗した場合は、`sidecar/src/test/resources/yo/dbunitcli/sidecar/controller/` 配下の期待値JSONを実際のレスポンスに合わせて更新する

## 2. tauri: フォームテストのフィクスチャを更新する

コントローラーのレスポンス構造（フィールド名・型・デフォルト値・selectOption）が変わった場合、Tauriのフォームテスト用フィクスチャを更新する。

### 更新対象ファイル

- `tauri/src/tests/app/form/fixtures.ts`

### 確認手順

1. 対応するフィクスチャ定数（`compareLoadResponseFixture`, `convertLoadResponseFixture` 等）を確認する
2. フィールド名・型・デフォルト値・`selectOption` 配列がコントローラーのレスポンスJSONと一致しているか検証する
3. 変更があれば `fixtures.ts` を更新し、以下のテストが通ることを確認する
   - `tauri/src/tests/app/form/CommandForm.test.tsx`
   - `tauri/src/tests/app/form/ConvertForm.test.tsx`

## 3. GUI: 影響箇所をコードレビューで確認する

GUIモジュールにはテストがないため、以下のファイルをコードレビューで確認する。

### 確認対象ファイル

- `gui/src/main/java/yo/dbunitcli/javafx/view/main/MainPresenter.java`

### 確認観点

- `Option` インターフェースや `ParamType` 列挙値を変更した場合、フィールド生成ロジック（`setInputFields` 等）が正しく動作するか確認する
- DTOのフィールド追加・削除・名称変更をした場合、GUI上のフォーム項目の表示が正しいことを目視確認する
- `CommandLineOption` や `Command` の基底クラスシグネチャを変更した場合は、コンパイルエラーがないことを確認する
