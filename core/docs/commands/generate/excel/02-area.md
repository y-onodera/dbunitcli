# Excel Area機能の詳細

## 概要
jxlsのArea（xlsArea）は、テンプレート内の特定のセル範囲を定義する基本単位です。
データの繰り返しや条件分岐などの処理は、このArea内で行われます。

## Area定義の方法

### 1. 基本的な定義
Areaは対象範囲の終了セルを指定して定義します：
```
jx:area(lastCell="B10")           // 現在のセルからB10までの範囲
```

### 2. コマンドと組み合わせ
各コマンドでもArea範囲を指定できます：
```
jx:each(items="rows" var="row" lastCell="D5")  // 繰り返し範囲の指定
jx:if(condition="amount > 1000" lastCell="C3")  // 条件付き範囲の指定
```

### 3. 入れ子の定義
Areaは階層的に定義することができます：
```
// A1セルのコメントに記述
jx:area(lastCell="E20")
  // B2セルのコメントに記述
  jx:each(..., lastCell="E10")
    // C3セルのコメントに記述
    jx:if(..., lastCell="E5")
```

## データ処理の制御

### 1. シフト制御
Areaの拡大・縮小を制御できます：
```
jx:area(lastCell="B10" shift="Down")  // 下方向へシフト
```

### 2. データサイズへの対応
処理データ量に応じてArea範囲が自動調整されます：
- データが少ない場合：余白を詰める
- データが多い場合：範囲を拡張

## 関連項目
- [テンプレートの基本](01-basic.md)
- [使用例](03-examples.md)
- [高度な使用方法](04-advanced.md)