# ビルドコマンド

## core ビルドコマンド

```powershell
# 通常のビルド
mvn clean compile

# テスト実行
mvn test

# 統合テスト実行
mvn test -PIntegrationTest

# ネイティブイメージビルド
mvn clean package -Pnative

# 全依存関係を含むJARビルド
mvn clean package
```

## テスト実行方法

### テストクラスの構造について

`GenerateTest` など各コマンドのテストクラスは複数のネストしたテストクラスを含んでいます：

- `GenerateTest$NoSystemPropertyTest`: システムプロパティなしでのテスト
- `GenerateTest$AllSystemPropertyTest`: すべてのシステムプロパティを設定してのテスト
- `GenerateTest$ChangeDataSetBaseTest`: データセットベースディレクトリを変更してのテスト
- など

特定のネストしたクラスのテストメソッドを実行する場合は `$` でクラスを区切り、`#` でメソッドを指定します。

### テストの実行例

```powershell
# coreディレクトリに移動
cd core

# 全テストを実行
mvn test

# 特定のテストクラスを実行
mvn test -Dtest=GenerateTest

# 特定のテストクラスの特定のメソッドを実行（ネストしたクラスの場合）
mvn test -Dtest='GenerateTest$NoSystemPropertyTest#testGenerateXlsx'
```

## 主要リソースファイルの場所

- テンプレート: `src/main/resources/settings/`
- テストデータ: `src/test/resources/yo/dbunitcli/application/`