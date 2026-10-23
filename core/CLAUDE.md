# DBUnit CLI Core

CSV・Excel・DB間のデータ比較・変換・生成のためのCLIツール。GraalVM Native Image としてビルド可能。

## 主要コマンド
- **Compare**: 複数データソース間の比較・差分レポート生成
- **Convert**: CSV ⇔ Excel ⇔ DB のシームレス変換
- **Generate**: StringTemplate 4 によるテンプレートベース生成（SQL・設定ファイル等）
- **Run**: SQL / ANT スクリプト / バッチファイルの実行
- **Parameterize**: 設定ファイルベースのバッチ処理
- **Scaffold**: Generate固定成果物型をカスタマイズするための雛型生成

## パッケージ構成

### `yo.dbunitcli.application`
- `command/` — 各コマンドの実装とコマンドDTO
- `dto/` — オプション用DTO、`json/` — JSON設定パーサー、`option/` — Picocli オプション

### `yo.dbunitcli.dataset`
- `compare/` — 差分検出・レポート生成
- `converter/` — フォーマット変換（CSV / XLS / XLSX / DB）
- `producer/` — 各入力タイプ用データソースプロデューサー
- `filter/` — データフィルタリング

### `yo.dbunitcli.resource`
- `jdbc/` — DB接続管理、`poi/` — Apache POI（Excel処理）、`st4/` — StringTemplate 4

## 対応データ形式

| 入力                                 | 出力                         |
|------------------------------------|----------------------------|
| CSV / TSV / Excel / 固定長 / 正規表現テキスト | CSV / TSV / Excel / DBテーブル |
| JDBC（テーブル・SQLクエリ）                  | テンプレートベース生成（SQL・テキスト等）     |
| 画像 / PDF（比較のみ）                     | —                          |

## 技術スタック
- DBUnit、Apache POI、StringTemplate 4、Picocli
- SLF4J + Logback、JUnit 5
- Java 25、GraalVM Native Image 対応

## テスト
ルートで `mvn test -pl core`（`-Dtest=クラス名$ネスト#メソッド` で絞り込み）。統合テストは `-PIntegrationTest`
