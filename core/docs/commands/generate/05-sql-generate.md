# SQLファイル生成

## 概要
データセットの内容に基づいて、各種操作（INSERT/DELETE/UPDATE等）のSQLファイルを生成します。
テーブル単位での生成に対応しており、コミット制御も可能です。

## 設定項目
| 引数 | 説明 | 必須 |
|------|------|------|
| -generateType | sql を指定 | ○ |
| -unit | table を指定（固定） | ○ |
| -operation, -op | SQL操作タイプ | ○ |
| -result | 出力先ディレクトリ | △ |
| -resultPath | 出力ファイルパス | △ |
| -outputEncoding | 出力ファイルの文字コード（デフォルト：UTF-8） | - |
| -commit | コミット有無（デフォルト: true） | - |
| -sqlFilePrefix | 生成SQLファイル名の接頭辞 | - |
| -sqlFileSuffix | 生成SQLファイル名の接尾辞 | - |

※ src.useJdbcMetaDataは自動的にtrueに設定されます

### 操作タイプ
* INSERT: レコード挿入
* DELETE: レコード削除
* UPDATE: レコード更新
* CLEAN_INSERT: 削除後に挿入
* DELETE_INSERT: 削除と挿入を別ファイルに出力

## 生成される成果物

### ファイル形式
* 拡張子: .sql
* エンコーディング: -outputEncodingで指定（デフォルトUTF-8）

### 出力先
* -resultDirで指定したディレクトリに生成
* ファイル名パターン：[prefix][テーブル名][suffix].sql
  - DELETE_INSERTの場合：
    - Delete_[テーブル名].sql
    - Insert_[テーブル名].sql

### コミット制御
* -commit=true（デフォルト）：
  ```sql
  DELETE FROM users WHERE id = 1;
  COMMIT;
  ```

* -commit=false：
  ```sql
  DELETE FROM users WHERE id = 1;
  ```

## 使用例
```bash
# INSERTのSQL生成（コミットあり）
dbunit generate -generateType sql \
  -unit table \
  -operation INSERT \
  -src.srcType table -src.src tables.txt \
  -resultDir ./sql

# DELETE_INSERTのSQL生成（コミットなし）
dbunit generate -generateType sql \
  -unit table \
  -operation DELETE_INSERT \
  -commit false \
  -src.srcType table -src.src tables.txt \
  -resultDir ./sql