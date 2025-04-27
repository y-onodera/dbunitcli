# データベース設定

データベースへの接続や操作に関する設定です。

## jdbc.* (データベース接続設定)

### プロパティファイルによる設定
* -jdbcProperties: JDBC接続プロパティファイルのパス
  - url: 接続URL
  - user: ユーザー名
  - pass: パスワード

### 個別指定による設定
* -jdbcUrl: JDBC接続URL
* -jdbcUser: データベースユーザー名
* -jdbcPass: データベースパスワード

## 使用例
```bash
# プロパティファイルを使用
dbunit run -scriptType sql \
  -jdbcProperties "config/database.properties"

# 個別に接続情報を指定
dbunit run -scriptType sql \
  -jdbcUrl "jdbc:mysql://localhost/testdb" \
  -jdbcUser "user" \
  -jdbcPass "pass"