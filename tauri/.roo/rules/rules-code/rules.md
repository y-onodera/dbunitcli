# Coreプロジェクト Codeモード開発ルール

このドキュメントは、Codeモード固有の開発ルールを定義します。

## 1. ロール定義

あなたは Tauri v2 + rust + TypeScript + react + Tailwindcss のエキスパートエンジニア兼 UI/UX デザイナーとして対応してください。

## 2. 実装修正時の注意点

1. 修正を検討しているファイルは、ファイルシステム上の実際のファイルの内容を確認してから修正方法を考える
2. 修正前に対象ファイルに関連する仕様ドキュメント(`docs/01-index.md`参照)を確認する

### 3. Biomeの設定

#### 自動フォーマット
- ファイル保存時に自動フォーマット
- コード入力時にリアルタイムフォーマット

#### フォーマットルール
```json
{
  "formatter": {
    "enabled": true,
    "indentStyle": "space",
    "indentWidth": 2,
    "lineWidth": 100
  },
  "linter": {
    "enabled": true,
    "rules": {
      "recommended": true,
      "correctness": {
        "noUnusedVariables": "error"
      },
      "suspicious": {
        "noExplicitAny": "error"
      },
      "style": {
        "useConst": "error",
        "noNonNullAssertion": "error"
      }
    }
  },
  "javascript": {
    "formatter": {
      "quoteStyle": "single",
      "trailingComma": "es5",
      "semicolons": "always"
    }
  }
}
```

### 4. コードスタイル

- 関数やコンポーネントには適切なコメントを含める
- 型のインポートには `import type` を使用
- インデントは2スペース
- 行の最大長は100文字
- シングルクォート使用
- 文末のセミコロンは必須
- 適切なnullチェックの実施
- 早期リターンパターンの活用
