# DBUnit CLI Test Mode Rules

このドキュメントは、`core`プロジェクトにおけるテストモードのルールを定義します
Junitを使ってテストを実装し、実装が終わったら作ったテストケースを実行して正常終了を確認します

## 1. テスト実装のガイドライン

src/main/javaの下に実装されたjavaソースと1:1でテストクラスを作成します
作業を始める前に必ずテスト対象のjavaソースの現在の実装を確認してください

## 1.1 使用可能なライブラリ
- JUnit Jupiter 5.10.1
  - junit-jupiter-api
  - junit-jupiter-engine
  - junit-jupiter-params
- JMockit 1.49 (モック作成用)

### 1.2 テストケース設計
- テストクラス：`対象クラス名 + Test`
- 1つのテストメソッドは1つの機能や条件のみを検証
- テストメソッド名は`テスト対象のメソッド名 + When + テストの条件`の形式
  - 例：`convertToCsvWhenEmptyDataSetShouldCreateEmptyFile`
  - 例：`parseJsonWhenInvalidJsonFormatShouldThrowJsonParseException`
  - 例：`exportToExcelWhenExceedMaxRowsShouldThrowIllegalArgumentException`
- テストデータは`src/test/resources`配下に配置

### 1.3 テストデータ管理
- テストデータは目的別にディレクトリを分類
- データセットは再利用可能な形で設計
- テストリソースの取得には`TestResourceLoader`インターフェースを使用
  - クラスパスからのリソース読み込みを統一的に処理
  - 相対パスでのリソース参照を可能に
- テストリソースはパッケージ構造に合わせて配置
  - テストクラスと同じパッケージ構造を`src/test/resources`配下に作成
  - JSONなどのテストリソースファイルもテストコードの一部として管理
  - 例：
    ```
    src/test/java/yo/dbunitcli/application/json/MyTest.java
    src/test/resources/yo/dbunitcli/application/json/MyTest/test_data.json
    ```

### 1.4 可視性
- テストクラス: public
- テストメソッド: public
- セットアップ/クリーンアップメソッド: protected
- ヘルパーメソッド: private

### 1.5 テストクラスの構造

```java
public class TargetTest {
    @BeforeEach
    protected void setUp() {
        // テスト前の共通セットアップ
    }

    @Test
    public void validateWhenMissingRequiredFieldShouldFail() {
        // 準備
        // 実行
        // 検証
    }

    @AfterEach
    protected void tearDown() {
        // テスト後の共通クリーンアップ
    }
}
```