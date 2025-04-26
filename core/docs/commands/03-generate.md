# Generateコマンド

## 概要
generateコマンドは、データセットの内容に基づいて様々な形式のファイルを生成します。
データベース構造やテンプレートを入力として、以下のような成果物を生成できます：

- テキストファイル - StringTemplate4による柔軟なテキスト生成
- Excelファイル - jxlsによる高度なExcel生成（xls/xlsx）
- 設定ファイル - データベース構造に基づく設定情報
- SQLファイル - データ操作用のSQL文

## 基本的な使用方法
```bash
dbunit generate -generateType <生成タイプ> -unit <生成単位> -template <テンプレート> -result <出力先>
```

### 主要な引数
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | 生成する形式（txt/xlsx/xls/settings/sql） | ○ |
| -unit | 生成単位（record/table/dataset） | ○ |
| -template | テンプレートファイルのパス | △ |
| -result | 出力先ディレクトリ | ○ |

※ △：generateTypeにより必須/任意が変わります

## 生成タイプ
各生成タイプの詳細は以下のドキュメントを参照してください：

- [テキストファイル生成](generate/02-txt-generate.md)
  - StringTemplate4によるテキスト生成
  - 柔軟なテンプレート構文
  - 多様な出力形式に対応

- [Excelファイル生成](generate/03-excel-generate.md)
  - jxlsによるExcelファイル生成
  - 複雑なレイアウトに対応
  - 数式の処理制御が可能

- [設定ファイル生成](generate/04-settings-generate.md)
  - DB構造から設定情報を生成
  - 主キー情報の自動抽出
  - カラム情報の制御

- [SQLファイル生成](generate/05-sql-generate.md)
  - データ操作SQLの生成
  - 複数の操作タイプに対応
  - コミット制御が可能

## 共通設定
generateコマンドの基本機能と共通引数の詳細については[基本機能と共通引数](generate/01-overview.md)を参照してください。

## 出力先の指定
生成されたファイルは以下のルールで出力されます：

1. -resultPathが指定された場合
   - 指定されたパスに直接出力

2. -resultDirが指定された場合
   - 生成タイプとunitに応じて自動的にファイル名を決定
   - 詳細は各生成タイプのドキュメントを参照