# DBUnit CLI Test Mode Rules

このドキュメントは、`core`プロジェクトにおけるテストモードのルールを定義します。

## 1. テストモードの役割

テストモードは以下の役割を担います：

- テストケースの設計と実装
- テストデータの準備と検証
- テスト実行環境の構築と管理
- テスト結果の分析とレポート

## 2. 必要な知識

テストモードには以下の知識が必要です：

- JUnitフレームワークの深い理解
- DBUnitフレームワークの使用経験
- データセット操作の知識
- データベーステストの設計パターン
- 各種データフォーマット（CSV、Excel、JSON等）の取り扱い

## 3. 使用可能なライブラリ

### 3.1 テストフレームワーク
- JUnit Jupiter 5.10.1
  - junit-jupiter-api
  - junit-jupiter-engine
  - junit-jupiter-params
- JMockit 1.49 (モック作成用)
- DBUnit 2.8.0 (データベーステスト用)
- Allure 2.25.0 (テストレポート生成用)

### 3.2 データベース
- H2 Database 2.3.232 (インメモリテストDB)
- Oracle JDBC (ojdbc11)
- PostgreSQL JDBC

## 4. アクセス制御

### 4.1 クラスの可視性
- テストクラス: public
- テストヘルパークラス: package-private
- テストユーティリティクラス: public (再利用可能な場合のみ)
- カスタムアサーションクラス: public

### 4.2 メソッドの可視性
- テストメソッド: public
- セットアップ/クリーンアップメソッド: protected
- ヘルパーメソッド: private
- カスタムアサーションメソッド: public（再利用可能な場合のみ）

## 5. テスト実装のガイドライン

### 5.1 テストケース設計
- 1つのテストメソッドは1つの機能や条件のみを検証
- テストメソッド名は`テスト対象のメソッド名 + When + テストの条件`の形式
  - 例：`convertToCsvWhenEmptyDataSetShouldCreateEmptyFile`
  - 例：`parseJsonWhenInvalidJsonFormatShouldThrowJsonParseException`
  - 例：`exportToExcelWhenExceedMaxRowsShouldThrowIllegalArgumentException`
- テストデータは`src/test/resources`配下に配置
- パラメータ化テストは`resources/yo/dbunitcli/application/param*.txt`に定義

### 5.2 テストデータ管理
- テストデータは目的別にディレクトリを分類
- データセットは再利用可能な形で設計
- 大規模なテストデータはバージョン管理対象外
- テストデータのエンコーディングはUTF-8を使用

### 5.3 アサーション
- 期待値と実際の値を明確に定義
- 複雑なデータ構造の比較はヘルパーメソッドを使用
- nullチェックを適切に実施
- 例外テストでは適切な例外クラスとメッセージを検証

### 5.4 テスト環境
- インメモリDBを優先的に使用
- 外部リソースへの依存は最小限に
- 環境依存の設定は外部ファイル化
- クリーンアップ処理の確実な実施

## 6. コーディング規約

### 6.1 命名規則
- テストクラス：`対象クラス名 + Test`
- テストメソッド：`メソッド名 + When + テストの条件`
  - 例：`executeWhenValidInputShouldSucceed`
  - 例：`validateWhenMissingRequiredFieldShouldFail`
  - 例：`convertWhenNullDataShouldThrowException`
- テストデータファイル：`TestTarget_Scenario.ext`
- ヘルパーメソッド：`functionNameHelper`

### 6.2 コメント規約
- テストクラスには目的を記載
- 複雑なテストシナリオは手順をコメントで説明
- テストデータの特殊な条件は明記
- 不具合修正に関連するテストはIssue番号を記載

### 6.3 構造化

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

## 7. レビュー基準

テストコードのレビューでは以下を確認：

- テストの目的と範囲が明確か
- テストデータが適切に準備されているか
- アサーションが適切に実装されているか
- テスト環境のセットアップとクリーンアップが適切か
- テストの実行時間が許容範囲内か