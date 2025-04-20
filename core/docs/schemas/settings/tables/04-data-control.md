# データの制御

テーブルのデータを制御するための基本的な機能について説明します。
その他、フィルタリング機能については[フィルタリング](05-filtering.md)を参照してください。

## ソート順の指定

order配列でソート順を指定します。指定したカラムで昇順でソートされます：
```json
{
  "settings": [
    {
      "name": "users",
      "order": [
        "created_at",
        "id"
      ]
    }
  ]
}
```

## カラムの選択

### 対象カラムの指定
include配列で処理対象のカラムを指定：
```json
{
  "settings": [
    {
      "name": "users",
      "include": [
        "id",
        "name",
        "email"
      ]
    }
  ]
}
```

### 除外カラムの指定
exclude配列で処理から除外するカラムを指定：
```json
{
  "settings": [
    {
      "name": "users",
      "exclude": [
        "password",
        "deleted_at"
      ]
    }
  ]
}
```

## 重複除去

distinctフラグで重複行を除去します：
```json
{
  "settings": [
    {
      "name": "access_logs",
      "keys": ["session_id"],
      "include": ["user_id", "action"],
      "distinct": true
    }
  ]
}
```

## 制御の組み合わせ

複数の制御を組み合わせて使用できます：
```json
{
  "settings": [
    {
      "name": "sales",
      "include": ["id", "amount", "date"],
      "order": ["date", "amount"],
      "distinct": true
    }
  ]
}
```