# フィルタータイプとJSON設定

テーブル名やシート名に対するフィルタリングをJSON設定ファイルで定義する方法を説明します。

## 1. フィルター形式の基本構造

フィルタリングは以下の2つの方式で指定できます：

### 1.1 完全一致指定

テーブル名との完全一致を判定します。

単一の名前を指定：
```json
{
    "name": "TABLE1"
}
```

複数の名前を指定：
```json
{
    "name": ["TABLE1", "TABLE2"]
}
```

### 1.2 パターン指定

様々なパターンでのマッチングを行います：

```json
{
    "pattern": {
        "フィルタータイプ": "フィルター条件"
    }
}
```

## 2. フィルタータイプの種類

### 2.1 正規表現 (RegexFilter)

正規表現によるパターンマッチングを提供します。

```json
{
    "pattern": {
        "regex": "^TABLE_[A-Z]+$"
    }
}
```

### 2.2 文字列一致

文字列の部分一致でフィルタリングを行います。

```json
{
    "pattern": {
        "string": "MASTER_"
    }
}
```

### 2.3 除外設定

特定のテーブルやパターンを除外します。

```json
{
    "pattern": {
        "regex": "^TABLE_.*",
        "exclude": ["TABLE_TEMP", "TABLE_BAK"]
    }
}
```

より詳細な使用例は[フィルター使用例](03-filter-examples.md)を参照してください。