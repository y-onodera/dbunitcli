# セルテーブルの使用例

セル単位でのテーブル定義の具体的な使用例を説明します。
基本的な構造については[セルテーブルスキーマ](02-cells-basic.md)を参照してください。

## 不規則データの定義

帳票形式のように項目が不規則に配置されているデータの定義：
```json
{
  "cells": [
    {
      "sheetName": "フォーム",
      "tableName": "form_data",
      "header": ["申請者", "部署", "承認者"],
      "rows": [
        {
          "cellAddress": ["B2", "D2", "F2"]
        },
        {
          "cellAddress": ["B5", "D5", "F5"]
        }
      ]
    }
  ]
}
```

## 複数のブロックデータ

一定の間隔で繰り返されるブロックデータの定義：
```json
{
  "cells": [
    {
      "sheetName": "集計",
      "tableName": "summary",
      "header": ["区分", "金額", "備考"],
      "rows": [
        {
          "cellAddress": ["B2", "C2", "D2"]
        },
        {
          "cellAddress": ["B10", "C10", "D10"]
        }
      ]
    }
  ]
}
```

## ファイル情報の追加

シート名やファイル名を含める場合：
```json
{
  "cells": [
    {
      "sheetName": "データ",
      "tableName": "data",
      "header": ["名称", "値"],
      "rows": [
        {
          "cellAddress": ["A1", "B1"]
        }
      ],
      "addFileInfo": true
    }
  ]
}
```

## 関連項目
- [セルテーブルスキーマ](02-cells-basic.md)
- [行単位のテーブル定義](01-rows.md)
- [複数テーブル形式](../format/02-multi.md)