# ResourcesクラスとResourceFileコントローラーの設計変更

## 目的
- jdbcとtemplateのリソース管理をResourceFile型に統一し、一貫したリソース管理を実現する
- 各リソースタイプに特化したコントローラーを提供する

## 構成

### 変更対象
1. yo.dbunitcli.sidecar.domain.project.Resources
   - jdbcとtemplateフィールドの型をList<String>からResourceFileに変更
   - 関連するBuilder、toDtoメソッドの修正

2. 新規コントローラー
   - JDBCリソース用コントローラー
   - テンプレートリソース用コントローラー

## サブタスク

### Codeモード用サブタスク
1. Resourcesクラスの修正 [code/001.md]
   - フィールド型の変更
   - Builderクラスの修正
   - toDtoメソッドの更新

2. リソースコントローラーの実装 [code/002.md]
   - JdbcResourceFileControllerの作成
   - TemplateResourceFileControllerの作成

### Docsモード用サブタスク
1. APIドキュメントの更新 [docs/001.md]
   - 新規エンドポイントの追加
   - リクエスト/レスポンス形式の記載

## 完了条件
1. jdbcとtemplateフィールドがResourceFile型で管理されている
2. 各リソースタイプに対応したコントローラーが実装されている
3. APIドキュメントが更新されている

## 依存関係
1. コードの修正を先に実施
2. コード修正完了後にドキュメントを更新