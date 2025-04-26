# テキストファイル生成

## 概要
StringTemplateV4（St4）テンプレートエンジンを使用してテキストファイルを生成します。
レコード単位、テーブル単位、またはデータセット単位での生成に対応しています。

## テンプレートファイル
テンプレートファイルはSt4の文法に従って記述します。
基本的な文法や使用例については以下のドキュメントを参照してください：

- [テンプレートの基本文法](../../options/template/04-syntax.md)
- [基本的な使用例](txt/02-basic-examples.md)
- [レコード単位の例](txt/03-record-examples.md)
- [テーブル単位の例](txt/04-table-examples.md)
- [データセット単位の例](txt/05-dataset-examples.md)

## 設定項目
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | txt を指定 | ○ |
| -template | テンプレートファイルパス | ○ |
| -unit | [処理単位](../../options/template/02-processing-units.md) | ○ |
| -template.* | [テンプレート設定](../../options/04-template.md) | - |

## 生成される成果物

### ファイル形式
* 拡張子: .txt
* エンコーディング: -outputEncodingで指定（デフォルトUTF-8）

### 出力先
* -resultPathで指定された場合：指定されたパスに生成
* -resultDirで指定された場合：
  - unit=record: [prefix]テーブル名_行番号[suffix].txt
  - unit=table: [prefix]テーブル名[suffix].txt
  - unit=dataset: [prefix]データセット名[suffix].txt

### 生成例
```
# unit=tableの場合の出力例
Table: USERS
+----+----------+------------------+
| ID | NAME     | EMAIL            |
+----+----------+------------------+
| 1  | user1    | user1@test.com   |
| 2  | user2    | user2@test.com   |
+----+----------+------------------+

# unit=recordの場合の出力例（USERS_1.txt）
ID: 1
NAME: user1
EMAIL: user1@test.com
```

## 使用例
```bash
# テーブル単位でテキストファイル生成
dbunit generate -generateType txt \
  -template table_template.txt \
  -unit table \
  -src.srcType table -src.src tables.txt \
  -resultDir ./output
```

※ tables.txtには対象テーブル名を記載します：
```
USERS
ROLES
PERMISSIONS