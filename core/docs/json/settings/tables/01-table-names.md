# テーブル名の指定方法

テーブルを特定するための様々な指定方法を説明します。
特定したテーブルに対してカラムの設定やソート・重複削除などのデータ制御、フィルタ、結合などのテーブル操作が可能です。
詳細なカラムの設定方法は[カラムの設定](03-column-settings.md)を参照してください。
詳細なデータ制御は[データ制御](04-data-control.md)を参照してください。
詳細なデータのフィルタ方法は[フィルタリング](05-filtering.md)を参照してください。
詳細なテーブル操作は[その他の操作](06-operations.md)を参照してください。

## 完全一致指定

単一のテーブルを指定：
```json
{
  "settings": [
    {
      "name": "users"            // テーブル名1
    },
    {
      "name": "tables"     // テーブル名2
    }
  ]
}
```

複数のテーブルを指定：
```json
{
  "settings": [
    {
      "name": [
        "users",
        "user_profiles",
        "tables"
      ]
    }
  ]
}
```

## パターンマッチング

テーブル名のパターンで指定：
```json
{
  "settings": [
    {
      "pattern": {
        "string": "mst_",       // プレフィックスマッチ
        "exclude": [            // 除外テーブル
          "mst_temp",
          "mst_backup"
        ]
      }
    }
  ]
}
```

### パターンマッチのルール
1. stringは部分一致（前方/後方/中間）
2. excludeで特定のテーブルを除外
3. 大文字小文字は区別する
