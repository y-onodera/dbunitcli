# パラメータ単位

データの処理単位を指定します。データセットの構造に応じて以下の3種類から選択できます。

## パラメータ単位の種類

### record
データセットの各行を個別のパラメータとして処理します。

- 1行ずつ処理が必要な場合に使用
- テンプレートの個別の値に注目
- 各行のデータに対して同じ処理を繰り返し実行

```bash
# 使用例
dbunit generate -generateType txt \
  -unit record \
  -template record_template.stg
```

### table
各テーブルを1つのパラメータとして処理します。

- 複数行をまとめて処理する場合に使用
- テーブル全体のデータを扱う
- 行のグループに対する処理

```bash
# 使用例
dbunit generate -generateType sql \
  -unit table \
  -template table_template.stg
```

### dataset
データセット全体を1つのパラメータとして処理します。

- 複数テーブルを一括処理する場合に使用
- テーブル間の関連を扱う
- データセット全体に対する処理

```bash
# 使用例
dbunit generate -generateType settings \
  -unit dataset \
  -template dataset_template.stg
```

各パラメータ単位のデータ構造の詳細は[データ構造](03-data-structures.md)を参照してください。