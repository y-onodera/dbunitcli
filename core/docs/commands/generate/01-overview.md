# generateコマンドの概要

## 基本機能
generateコマンドは、データセットの内容を基に各種ファイルを生成します。

## generateType一覧

### txt - テキストファイル生成
- カスタムテンプレート使用可能
- StringTemplate4エンジン使用
- エンコーディング指定可能
- 詳細は[テキストファイル生成](02-txt-generate.md)参照

### xlsx/xls - Excelファイル生成
- jxlsテンプレートエンジン使用
- 数式処理オプション対応
- セル書式の保持
- 詳細は[Excelファイル生成](03-excel-generate.md)参照

### settings - 設定ファイル生成
- 固定テンプレート使用
- JSON形式で出力
- includeAllColumns対応
- 詳細は[設定ファイル生成](04-settings-generate.md)参照

### sql - SQLファイル生成
- 固定テンプレート使用
- 複数の操作タイプに対応
- コミット制御可能
- 詳細は[SQLファイル生成](05-sql-generate.md)参照

## 共通設定項目
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | 生成するファイル形式を指定 | ○ |
| -template | テンプレートファイルのパス | △ |
| -unit | [処理単位](../template/02-processing-units.md)を指定 | ○ |
| -result | 出力先ディレクトリ | ○ |
| -resultPath | 出力ファイルパス | - |
| -template.* | [テンプレート設定](../../options/04-template.md) | - |

## 基本的な使用例
```bash
# テキストファイル生成
dbunit generate -generateType txt -template template.txt -unit table \
  -src.srcType table -src.src tables.txt -resultDir ./output

# Excelファイル生成
dbunit generate -generateType xlsx -template template.xlsx -unit dataset \
  -src.srcType table -src.src tables.txt -resultDir ./output
```

## 関連項目
- [テンプレート処理の概要](../template/01-overview.md)
- [処理単位](../template/02-processing-units.md)
- [データ構造](../template/03-data-structures.md)