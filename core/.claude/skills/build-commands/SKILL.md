---
description: core モジュールのビルド・テストコマンドリファレンス。
---

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

## テストの実行例

ネストしたクラスは `$` で区切り、メソッドは `#` で指定します。

```powershell
# 全テストを実行
mvn test

# 特定のテストクラスを実行
mvn test -Dtest=GenerateTest

# ネストしたクラスの特定メソッドを実行
mvn test -Dtest='GenerateTest$NoSystemPropertyTest#testGenerateXlsx'
```

## 主要リソースファイルの場所

- テンプレート: `src/main/resources/settings/`
- テストデータ: `src/test/resources/yo/dbunitcli/application/`
