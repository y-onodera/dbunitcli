# テンプレート設定

テンプレートの処理に関する設定です。テンプレートファイルの読み込みや変数の制御などの設定が含まれます。

## TemplateRenderOption (template.*)

### 基本設定
* -template: テンプレートファイルのパス
* -templateGroup: テンプレートグループファイルのパス
* -encoding: テンプレートのエンコーディング
* -formulaProcess: Excel数式処理の有効化（xlsxのみ）
* -evaluateFormulas: Excel数式の評価制御（デフォルト: true）
  - formulaProcessがtrueの場合のみ有効
  - falseの場合は数式がそのまま保持される

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

## Excel数式の処理

### 数式処理の制御
Excel出力時の数式処理は以下の2つのオプションで制御されます：

1. formulaProcess
   - 数式処理機能の有効/無効を制御
   - falseの場合、メモリ使用量を抑制できるが、行の増減に応じたセル参照の自動調整が無効

2. evaluateFormulas
   - 数式の評価有無を制御
   - formulaProcessがtrueの場合のみ有効
   - falseの場合は数式をそのまま保持（計算結果への置換を行わない）

### 使用例
```bash
# 数式を評価して結果を出力
dbunit generate -generateType xlsx \
  -template.formulaProcess true \
  -evaluateFormulas true

# 数式をそのまま保持
dbunit generate -generateType xlsx \
  -template.formulaProcess true \
  -evaluateFormulas false
```

詳細な使用例は以下を参照：
   - [テンプレート処理](template/01-overview.md)
