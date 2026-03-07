# ビルドコマンド

## sidecar ビルドコマンド

```bash
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

## 注意事項

- sidecar のテスト実行前に core を先にビルドしておく必要がある
  ```bash
  mvn install -pl core -Dmaven.test.skip=true
  ```