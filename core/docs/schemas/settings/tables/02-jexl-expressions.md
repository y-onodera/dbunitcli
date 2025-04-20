# JExl式の利用

このツールでは以下の機能でApache Commons JExlの式を使用します：
- データのフィルタリング
- カラム型の指定
- テーブルの結合条件
- カラム値の動的生成

## 基本構文

### 演算子
- 比較演算子: ==, !=, >, <, >=, <=
- 論理演算子: &&（AND）, ||（OR）, !（NOT）
- 文字列演算子: +（連結）
- 算術演算子: +, -, *, /, %

### 文字列操作
- contains(str): 指定文字列を含むか
- startsWith(prefix): 指定文字列で始まるか
- endsWith(suffix): 指定文字列で終わるか
- matches(regex): 正規表現にマッチするか
- substring(start,end): 部分文字列の取得

### その他
- NULL比較: == null, != null
- 文字列リテラル: 単一引用符で囲む（'text'）
- 三項演算子: condition ? value1 : value2

## データ制御での使用

フィルター条件の指定：
```json
{
  "settings": [
    {
      "name": "orders",
      "filter": [
        "amount > 1000 && status == 'APPROVED'",
        "type != null && type.startsWith('SALE')"
      ]
    }
  ]
}
```

## カラム型の指定での使用

条件式を使ったboolean型の定義：
```json
{
  "settings": [
    {
      "name": "table1",
      "boolean": {
        "is_amount": "col.endsWith('_amount')"
      }
    }
  ]
}
```

## テーブル結合での使用

結合条件の指定：
```json
{
  "settings": [
    {
      "innerJoin": {
        "left": "orders",
        "right": "items",
        "on": "orders_item_id == items_id"
      }
    }
  ]
}
```
結合時のカラム参照は"{テーブル名}_{カラム名}"の形式を使用します。

## 注意事項

1. 文字列比較は大文字小文字を区別します
2. 日付比較は文字列として行われます
3. NULL値との比較は == null / != null を使用
4. 複雑な条件は複数の式に分割することを推奨