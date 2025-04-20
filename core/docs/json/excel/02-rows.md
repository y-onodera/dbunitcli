# Excel行テーブルスキーマ

Excel形式でテーブルを行単位で定義する方法を説明します。

## スキーマ構造

```json
{
  "rows": [
    {
      "sheetName": "シート1",       // シート名
      "tableName": "テーブル1",     // 生成されるテーブル名
      "header": [                   // カラム名の配列
        "id",
        "name",
        "value"
      ],
      "dataStart": 2,              // データ開始行（0ベース）
      "columnIndex": [             // 列インデックス（0ベース）
        0,  // id列
        1,  // name列
        2   // value列
      ],
      "breakKey": [               // テーブル分割用のキー
        "id"
      ],
      "addFileInfo": false        // ファイル情報の付加
    }
  ]
}
```

## パラメータの説明

### 必須パラメータ
- **sheetName**: 対象のシート名
- **tableName**: 生成されるテーブルの名前
- **header**: カラム名の配列
- **dataStart**: データが始まる行の位置（0ベース）
- **columnIndex**: 各カラムのExcel上の列位置（0ベース）

### オプショナルパラメータ
- **breakKey**: テーブルを分割する際のキーとなるカラム
- **addFileInfo**: ファイル名等の情報をカラムとして追加（デフォルト：false）

## 使用例

### 単純なテーブル定義
```json
{
  "rows": [
    {
      "sheetName": "社員一覧",
      "tableName": "employees",
      "header": ["社員ID", "氏名", "部署"],
      "dataStart": 1,
      "columnIndex": [0, 1, 2]
    }
  ]
}
```

### 複数のテーブル定義
```json
{
  "rows": [
    {
      "sheetName": "マスタ",
      "tableName": "departments",
      "header": ["部署ID", "部署名"],
      "dataStart": 1,
      "columnIndex": [0, 1]
    },
    {
      "sheetName": "マスタ",
      "tableName": "employees",
      "header": ["社員ID", "氏名", "部署ID"],
      "dataStart": 1,
      "columnIndex": [3, 4, 5]
    }
  ]
}
```

## 関連項目
- [セル単位のテーブル定義](02-cells.md)
- [Excel基本形式](../format/01-basic.md)
- [複数テーブル形式](../format/02-multi.md)