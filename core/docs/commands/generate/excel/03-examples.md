# Excelテンプレート使用例

## 概要
パラメータ単位別のテンプレート作成例を説明します。
各例ではセルコメントを使用したマークアップ方法を採用しています。

## record単位のテンプレート

### 1. 基本レイアウト
```
// A1セルのコメントに記述
jx:area(lastCell="B5")

// セルの表示内容
受注情報
受注番号: ${record.id}
顧客名: ${record.customerName}
金額: ${record.amount}
```

### 2. 条件付きレイアウト
```
// A1セルのコメントに記述
jx:area(lastCell="C5")

// B2セルのコメントに記述
jx:if(condition="record.amount > 10000")
高額注文 | ${record.id} | ${record.amount}
```

## table単位のテンプレート

### 1. リスト形式
```
// A1セルのコメントに記述
jx:area(lastCell="D10")

// B2セルのコメントに記述
jx:each(items="rows" var="row" lastCell="D5")

顧客一覧
ID | 氏名 | メールアドレス
${row.id} | ${row.name} | ${row.email}
```

### 2. グループ化
```
// A1セルのコメントに記述
jx:area(lastCell="E15")

// B2セルのコメントに記述
部門別社員一覧
jx:each(items="rows.groupBy('department')" var="dept" lastCell="E5")
  部門: ${dept.key}
  jx:each(items="dept.rows" var="row")
    ${row.id} | ${row.name} | ${row.position}
```

## 関連項目
- [テンプレートの基本](01-basic.md)
- [Area機能の詳細](02-area.md)
- [高度な使用方法](04-advanced.md)