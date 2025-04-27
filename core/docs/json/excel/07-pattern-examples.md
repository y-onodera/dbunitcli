# シートパターンマッチングの使用例

## 1. 月次データシートの一括定義

複数の月次データシートを同じテーブル構造で定義する例です。

```json
{
    "patterns": {
        "monthly": "data_2024"  // 2024年のデータシートにマッチ
    },
    "rows": [
        {
            "sheetName": "monthly",
            "tableName": "MONTHLY_DATA",
            "header": ["date", "amount", "category"],
            "columnIndex": [0, 1, 2],
            "dataStart": 1
        }
    ]
}
```

このパターンは以下のようなシートにマッチします：
- data_202401
- data_202402
- data_202403

## 2. 地域別データの定義

特定の地域コードを持つシートを定義する例です。

```json
{
    "patterns": {
        "regional": ["jp", "us", "uk"]  // 特定の地域コードを指定
    },
    "rows": [
        {
            "sheetName": "regional",
            "tableName": "REGIONAL_DATA",
            "header": ["region", "sales", "profit"],
            "columnIndex": [0, 1, 2],
            "dataStart": 1
        }
    ]
}
```

このパターンは以下のシートのみにマッチします：
- jp
- us
- uk

## 3. 一時ファイルを除外した集計

一時ファイルを除外してデータを集計する例です。

```json
{
    "patterns": {
        "summary": {
            "string": "summary_",
            "exclude": [
                "summary_temp",
                "summary_bak"
            ]
        }
    },
    "rows": [
        {
            "sheetName": "summary",
            "tableName": "SUMMARY_DATA",
            "header": ["item", "count", "total"],
            "columnIndex": [0, 1, 2],
            "dataStart": 1
        }
    ]
}
```

このパターンは以下のように動作します：
- summary_で始まるシートにマッチ
- ただしsummary_tempとsummary_bakは除外