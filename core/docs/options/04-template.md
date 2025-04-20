# テンプレート設定

テンプレートの処理に関する設定です。テンプレートファイルの読み込みや変数の制御などの設定が含まれます。

## TemplateRenderOption (template.*)

### 基本設定
* -template: テンプレートファイルのパス
* -templateGroup: テンプレートグループファイルのパス
* -encoding: テンプレートのエンコーディング
* -formulaProcess: Excel数式処理の有効化（xlsxのみ）

### テンプレート変数の制御
* -templateParameterAttribute: テンプレートパラメータの属性名（デフォルト: "param"）
* -templateVarStart: テンプレート変数の開始文字（デフォルト: '$'）
* -templateVarStop: テンプレート変数の終了文字（デフォルト: '$'）

## 使用例

### 基本的なテンプレート処理
```bash
dbunit generate -generateType txt \
  -template template.stg \
  -template.encoding "UTF-8" \
  -template.templateParameterAttribute "data"
```

### テンプレートグループの使用
```bash
dbunit generate -generateType txt \
  -templateGroup "templates/common.stg" \
  -templateName "generateSQL"
```

### カスタム変数記号の使用
```bash
# @variable@ 形式で参照
dbunit generate -generateType txt \
  -template query.stg \
  -template.templateVarStart "@" \
  -template.templateVarStop "@"
```

## 注意事項
- テンプレートファイルのエンコーディングを正しく指定
- 大きなデータセットを扱う場合はメモリ使用に注意
- テンプレート変数の記号が他の部分と競合しないよう注意

詳細な使用例は以下を参照：
- [テンプレート構文](../commands/template/04-syntax.md)
- [基本的な例](../commands/template/05-basic-examples.md)
- [共通利用ガイド](../commands/template/10-common-usage.md)