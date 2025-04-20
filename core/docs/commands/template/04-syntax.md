# テンプレート構文

dbunitcliのテンプレート処理はStringTemplate 4（ST4）を使用しています。

## 基本構文

### 変数の参照
```
$変数名$                // 単純な変数
$object.attribute$     // 属性の参照
```

### 条件分岐
```
$if(condition)$
  条件が真の場合の出力
$else$
  条件が偽の場合の出力
$endif$
```

### 繰り返し
```
$items:{item | $item.name$}; separator=","$
```

## データ型と書式

### 文字列
```
$string:option$      // upper, lower, cap, urlEncode
```

### 数値
```
$number:format$      // 書式指定（例：%.2f）
```

### 日付時刻
```
$date:format$        // yyyy-MM-dd など
```

### 配列/リスト
```
$list:option$        // length, first, last, rest
```

## テンプレート連携
```
$template(param1)$   // 別テンプレート呼び出し
$subTemplate()$      // サブテンプレート
```

## 特殊文字
* `$`: `$$`でエスケープ
* `<`,`>`: `\<`,`\>`でエスケープ
* `\`: `\\`でエスケープ

## コメント
```
$! This is a comment !$
```

型の定義や使用方法の詳細は以下を参照：
- [データ型の定義](../../json/settings/tables/types/01-data-types.md)
- [テンプレート機能概要](01-overview.md)
