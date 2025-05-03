# Excelファイル生成

## 概要
jxlsテンプレートエンジンを使用し、Excelテンプレートファイルにデータを適用して新しいExcelファイルを生成します。
.xlsxと.xls形式に対応しており、数式の処理も制御可能です。

## 設定項目
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | xlsx または xls を指定 | ○ |
| -template | テンプレートExcelファイルパス | ○ |
| -template.* | [テンプレート設定](../../options/05-template.md) | - |
| -unit | [処理単位](../../options/02-processing-units.md)を指定 | ○ |
| -result | 出力先ディレクトリ | ○ |
| -resultPath | 出力ファイルパス | - |

## 生成される成果物

### ファイル形式
* 拡張子: .xlsx または .xls
* ファイル構成:
  - unit=record: データごとに1ファイル
  - unit=table: テーブルごとに1ファイル
  - unit=dataset: 全データを1ファイルに集約

### 出力先
* -resultPathで指定された場合：指定されたパスに生成
* -resultDirで指定された場合：
  - unit=record: [テーブル名]_[行番号].xlsx
  - unit=table: [テーブル名].xlsx
  - unit=dataset: [データセット名].xlsx

### 数式の扱い
* template.evaluateFormulas=true（デフォルト）：
  - 数式を評価した結果を出力
  - SUM関数やVLOOKUPなどの計算結果が反映される
* template.evaluateFormulas=false：
  - 数式をそのまま保持
  - テンプレートの数式がそのまま出力ファイルに保持される

詳しい数式の処理については[テンプレート設定の数式処理](../../options/05-template.md#excel数式の処理)を参照してください。

## 使用例
```bash
# テーブル単位でExcelファイル生成
dbunit generate -generateType xlsx \
  -template template.xlsx \
  -unit table \
  -src.srcType table -src.src tables.txt \
  -resultDir ./output
```

## 関連項目
- [テンプレートの基本](excel/01-basic.md)
- [Area機能の詳細](excel/02-area.md)
- [使用例](excel/03-examples.md)
- [高度な使用方法](excel/04-advanced.md)