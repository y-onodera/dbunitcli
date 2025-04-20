# データベース設定

データベースへの接続や操作に関する設定です。JDBC接続情報の指定方法や、SQL実行時のテンプレート処理などの設定が含まれます。

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

## TemplateRenderOption (template.*)

### 基本設定
* -encoding: テンプレートのエンコーディング
* -formulaProcess: 数式処理の有効化（Excel関連）
* -templateGroup: テンプレートグループファイルのパス

### テンプレート変数の制御
* -templateParameterAttribute: テンプレートパラメータの属性名（デフォルト: "param"）
* -templateVarStart: テンプレート変数の開始文字（デフォルト: '$'）
* -templateVarStop: テンプレート変数の終了文字（デフォルト: '$'）

## 使用例
```bash
# 基本的なテンプレート処理
dbunit generate -generateType txt \
  -template.encoding "UTF-8" \
  -template.templateGroup "templates/common.stg" \
  -template.templateParameterAttribute "data"

# カスタム変数記号を使用
dbunit generate -generateType txt \
  -template.templateVarStart "@" \
  -template.templateVarStop "@" \
  -template.templateGroup "templates/custom.stg"
```

## 注意事項
- データベース接続情報は適切に管理し、機密情報の漏洩に注意してください
- テンプレート処理時のメモリ使用量に注意してください
- 大量のデータを扱う場合は、適切なバッチサイズを設定してください