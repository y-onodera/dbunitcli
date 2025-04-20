# Docsモードへの作業依頼

## 作業の進め方
1. 各セクションのドキュメントを順次作成
2. 100行を超える場合は分割を検討
3. 実装に基づく正確な記述を維持

## 1. 基本構造の作成
docs/schemas/01-*.mdを作成：

- [x] 01-table-settings.md: 全体構造の概要
- [ ] 01-settings-structure.md: 設定ファイルの詳細構造
- [ ] 01-settings-examples.md: 具体的な使用例
- [ ] 01-common-settings.md: commonSettingsとimport

## 2. データ型の説明
docs/schemas/02-*.mdを作成：

- [ ] 02-data-types.md: 型の基本概念
- [ ] 02-string-number.md: string型とnumber型
- [ ] 02-boolean-sql.md: boolean型とsqlFunction

## 3. カラム制御の解説
docs/schemas/03-*.mdを作成：

- [ ] 03-column-settings.md: カラム制御の基本
- [ ] 03-table-names.md: テーブル名指定方法
- [ ] 03-data-control.md: データ制御

## 4. テーブル操作の説明
docs/schemas/04-*.mdを作成：

- [ ] 04-table-operations.md: 操作の概要
- [ ] 04-split-separate.md: 分割と分離
- [ ] 04-table-join.md: テーブル結合

## 5. コマンドでの利用
docs/schemas/05-*.mdを作成：

- [ ] 05-generate-usage.md: Generate時の設定
- [ ] 05-compare-usage.md: Compare時の設定
- [ ] 05-dataload-usage.md: データソースでの設定

## 各ドキュメントの記載事項
1. 必ず含める内容：
   - 機能の詳細な説明
   - 具体的なJSONサンプル
   - 実用的な使用例

2. 必要に応じて含める内容：
   - エラーケース
   - アンチパターン
   - 他の機能との連携

3. 形式上の注意：
   - 100行以内に収める
   - 複雑な内容は分割検討
   - 実装に基づく正確な記述
