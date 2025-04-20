# テンプレート構文

dbunitcliのテンプレート処理はStringTemplate 4（ST4）を使用しています。

## 基本構文

### 変数の参照
```
$変数名$
Hello, $userName$!
```

### 属性の参照
```
$object.attribute$
$user.firstName$
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
$items:{item | 
  $item.name$
}; separator=","$
```

## データ型別の参照

### 文字列
```
$string:option$ // オプション: upper, lower, cap, urlEncode
$name:upper$    // 大文字変換
```

### 数値
```
$number:format$ // 書式指定
$price:%.2f$    // 小数点2桁
```

### 日付時刻
```
$date:format$ // 書式指定
$now:yyyy-MM-dd$ // 日付フォーマット
```

### 配列/リスト
```
$list:option$ // オプション: length, first, last, rest
$items:length$ // 要素数
```

## テンプレート間の連携

### 別テンプレートの呼び出し
```
$template(param1, param2)$
$subTemplate()$
```

### テンプレートの引数
```
templateName(arg1, arg2) ::= <<
  $arg1$ and $arg2$
>>
```

## エスケープ文字
* `$`: `$$`でエスケープ
* `<`: `\<`でエスケープ
* `>`: `\>`でエスケープ
* `\`: `\\`でエスケープ

## コメント
```
$! This is a comment !$
```

## 組み込み関数
* first: 最初の要素を取得
* last: 最後の要素を取得
* rest: 最初の要素以外を取得
* reverse: 順序を反転
* length: 要素数を取得
* strip: 前後の空白を削除

詳細な使用例は[テンプレート例](05-basic-examples.md)を参照してください。