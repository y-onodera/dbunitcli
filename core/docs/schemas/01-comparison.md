# 比較設定ファイル (setting)

Compareコマンドで使用され、テーブル間の比較方法を定義します。

## スキーマ構造

```json
{
  "settings": [
    {
      "name": "テーブル名（完全一致）",
      "pattern": {
        "string": "テーブル名（部分一致）",
        "exclude": ["除外テーブル名の配列"]
      },
      "keys": ["主キーカラム名の配列"],
      "include": ["含めるカラム名の配列"],
      "exclude": ["除外するカラム名の配列"],
      "order": ["ソート順のカラム名配列"],
      "filter": ["フィルター条件の配列"],
      "distinct": true,
      "tableName": "出力テーブル名",
      "prefix": "出力テーブル名のプレフィックス",
      "suffix": "出力テーブル名のサフィックス",
      "split": {
        "limit": 分割レコード数,
        "tableName": "分割テーブル名",
        "prefix": "分割テーブル名のプレフィックス",
        "suffix": "分割テーブル名のサフィックス",
        "breakKey": ["分割キーとなるカラム名の配列"]
      },
      "string": {
        "カラム名": "文字列変換式"
      },
      "number": {
        "カラム名": "数値変換式"
      },
      "boolean": {
        "カラム名": "真偽値変換式"
      },
      "sqlFunction": {
        "カラム名": "SQL関数式"
      }
    }
  ],
  "commonSettings": [
    {
      // settings配列と同じ形式の共通設定
    }
  ],
  "import": [
    {
      "path": "インポートする設定ファイルのパス"
    }
  ]
}
```

## テーブルの結合・分割設定

```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "左テーブル名",
        "right": "右テーブル名",
        "column": ["結合キーとなるカラム名の配列"],
        // または
        "on": "結合条件式"
      },
      // または
      "outerJoin": {
        // innerJoinと同じ形式
      },
      // または
      "fullJoin": {
        // innerJoinと同じ形式
      },
      "separate": [
        // settings配列と同じ形式の分割設定
      ]
    }
  ]
}