# Excelスキーマファイル (xlsxSchema)

Excelスキーマファイルは、Excel形式のデータセットを読み込む際に使用され、シートのデータ構造を定義します。

## スキーマ構造

```json
{
  "rows": [
    {
      "sheetName": "シート名",
      "tableName": "テーブル名",
      "header": ["カラム名の配列"],
      "dataStart": 開始行番号,
      "columnIndex": [カラムのインデックス配列],
      "breakKey": ["分割キーとなるカラム名の配列"],
      "addFileInfo": true
    }
  ],
  "cells": [
    {
      "sheetName": "シート名",
      "tableName": "テーブル名",
      "header": ["カラム名の配列"],
      "rows": [
        {
          "cellAddress": ["セルのアドレス配列"]
        }
      ],
      "addFileInfo": true
    }
  ]
}
```

## フィールドの説明

### rows セクション
* sheetName: データを読み取るシート名
* tableName: データを格納するテーブル名
* header: カラム名の配列
* dataStart: データの開始行番号（1から開始）
* columnIndex: 読み取るカラムのインデックス配列（0から開始）
* breakKey: データを分割する際のキーとなるカラム名
* addFileInfo: ファイル情報を追加するかどうか

### cells セクション
* sheetName: データを読み取るシート名
* tableName: データを格納するテーブル名
* header: カラム名の配列
* rows: 読み取るセルの情報
  * cellAddress: 読み取るセルのアドレス配列（例: ["A1", "B1", "C1"]）
* addFileInfo: ファイル情報を追加するかどうか

## 使用例

```json
{
  "rows": [
    {
      "sheetName": "顧客データ",
      "tableName": "customers",
      "header": ["id", "name", "email"],
      "dataStart": 2,
      "columnIndex": [0, 1, 2],
      "addFileInfo": true
    }
  ],
  "cells": [
    {
      "sheetName": "集計データ",
      "tableName": "summary",
      "header": ["total", "average", "count"],
      "rows": [
        {
          "cellAddress": ["B5", "B6", "B7"]
        }
      ]
    }
  ]
}