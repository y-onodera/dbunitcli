# フィルター設定例

フィルタータイプの具体的な使用例を説明します。

## 1. Excel構造定義での使用例

### 1.1 正規表現と除外の組み合わせ
```json
{
    "patterns": {
        "tableDef": {
            "regex": "^DATA_\\d{4}$",
            "exclude": ["DATA_0000", "DATA_9999"]
        }
    }
}
```

### 1.2 文字列一致と除外の組み合わせ
```json
{
    "patterns": {
        "masterDef": {
            "string": "MASTER",
            "exclude": ["OLD_MASTER", "TMP_MASTER"]
        }
    }
}
```

## 2. テーブル設定での使用例

### 2.1 完全一致
```json
{
    "settings": [{
        "name": "USER_MASTER",
        "keys": ["user_id"]
    }]
}
```

### 2.2 正規表現と除外の組み合わせ
```json
{
    "settings": [{
        "pattern": {
            "regex": "^TBL_\\d{6}$",
            "exclude": ["TBL_000000"]
        },
        "keys": ["id"]
    }]
}
```

### 2.3 文字列一致と除外の組み合わせ
```json
{
    "settings": [{
        "pattern": {
            "string": "MASTER",
            "exclude": ["OLD_MASTER"]
        },
        "keys": ["id"]
    }]
}
```

### 2.4 複数テーブルの指定
```json
{
    "settings": [{
        "name": ["TABLE1", "TABLE2"],
        "keys": ["id"]
    }]
}