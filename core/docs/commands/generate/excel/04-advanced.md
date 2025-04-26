# Excel生成の高度な使用方法

## 概要
Excel生成で利用可能な高度な設定について説明します。

## 大規模データの処理

### 1. 数式評価の制御
evaluateFormulasオプションで数式の評価タイミングを制御できます：

```bash
# デフォルト：保存時に数式を評価
-template.evaluateFormulas true
=SUM(B2:B10) → 生成時に計算して結果を保存

# 数式評価を無効化して処理を高速化
-template.evaluateFormulas false
=SUM(B2:B10) → 数式を保存し、Excel表示時に計算
```

### 2. xlsx形式の数式処理
formulaProcessオプションでxlsx形式の数式処理を制御できます：

```bash
# デフォルト：数式処理を実行
-template.formulaProcess true
jx:eachで行を繰り返した時に数式の行番号が自動更新される

# 数式処理を無効化してメモリ使用を最適化
-template.formulaProcess false
数式の解析をスキップし、処理効率が向上するが行番号は固定
```

### 3. each内の数式の違い
数式処理の有効/無効で動作が異なります：

```
// formulaProcess=true
jx:each(items="rows" var="row")
=SUM(B2:D2)  // 行が追加されると B3:D3, B4:D4 と行番号が更新

// formulaProcess=false
jx:each(items="rows" var="row")
=SUM(B2:D2)  // 全ての行で B2:D2 のまま（処理は高速）
```

大量データを扱う場合は、数式処理を無効化することで処理効率が改善します。
数式はExcelファイルを開いたときに自動的に計算されます。

## テンプレートパラメータの設定
パラメータのマッピング方法を制御できます：
```bash
# パラメータ属性の指定
-template.parameterAttribute "customParam"
```

## 関連項目
- [テンプレートの基本](01-basic.md)
- [Area機能の詳細](02-area.md)
- [使用例](03-examples.md)