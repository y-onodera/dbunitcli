# 設定ファイルの構造

テーブル設定ファイルの詳細な構造を説明します。

## 基本構造

### settings配列
テーブルごとの個別設定を定義します。

```json
{
  "settings": [
    {
      // 必須項目
      "name": "テーブル名",     // スキーマ名を含む場合: "schema.table"
      "keys": ["主キー"],      // 複数指定: ["key1", "key2"]

      // 任意項目
      "include": ["col1"],    // 処理対象のカラム
      "exclude": ["col2"],    // 除外するカラム
      "order": ["col3"],      // ソート順の指定
      "filter": ["条件式"],    // データ選択条件
      "distinct": true        // 重複除去フラグ
    }
  ]
}
```

### commonSettings配列
複数のテーブルで共有する設定を定義します。

```json
{
  "commonSettings": [
    {
      // settingsと同じ形式で定義
      "exclude": ["created_at", "updated_at"],
      "string": ["name", "email"],
      "number": ["id", "amount"]
    }
  ]
}
```

#### 上書きのルール
1. settingsの設定が優先
2. 配列は結合（include, excludeなど）
3. フラグは上書き（distinct）

### import配列
外部ファイルを参照します。

```json
{
  "import": [
    {
      "path": "./common/base.json"    // 相対パス
    },
    {
      "path": "/abs/path/conf.json"   // 絶対パス
    }
  ]
}
```

#### パス解決の優先順位
1. 絶対パス
2. 設定ファイルからの相対パス
3. カレントディレクトリからの相対パス

## テーブル操作の設定

テーブル操作設定では以下の機能を使用できます：
- [テーブル名の指定](tables/01-table-names.md)
- [JExl式の利用](tables/02-jexl-expressions.md)
- [カラムの設定](tables/03-column-settings.md)：使用可能なデータ型は[データ型の一覧](tables/types/01-data-types.md)を参照
  - 文字列型と数値型 - [詳細](tables/types/02-string-number.md)
  - 真偽値とSQL関数 - [詳細](tables/types/03-boolean-sql.md)
- [データの制御](tables/04-data-control.md)
- [フィルタリング](tables/05-filtering.md)
- [その他の操作](tables/06-operations.md)
- [分割と分離](tables/07-split-separate.md)
- [テーブル結合](tables/08-table-join.md)
- [結合条件](tables/09-join-conditions.md)

## 関連するコマンド
テーブル操作設定は以下のコマンドで使用します：
- [共通(データロード)](command/01-load.md)
- [generate](command/02-generate.md)
- [compare](command/03-compare.md)

設定例については以下を参照してください：
- [基本的な設定例](02-examples.md)
- [高度な設定例](03-advanced-examples.md)