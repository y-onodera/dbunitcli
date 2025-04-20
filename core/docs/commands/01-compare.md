# Compareコマンド

## 基本機能
2つのデータセット間の比較を行います。データの内容比較、画像の差分検出、PDFの比較など、様々な形式の比較に対応しています。比較結果は指定された形式で出力されます。

## 引数
* -targetType: 比較対象のタイプ(data/image/pdf) - [詳細な設定](#targettype)
* -setting: 比較設定ファイルパス - [スキーマ定義](../schemas/01-comparison.md)
* -settingEncoding: 設定ファイルのエンコーディング
* new.* : 新データセットの設定 - [データソース設定](../options/01-data-source.md)
* old.* : 旧データセットの設定 - [データソース設定](../options/01-data-source.md)
* expect.* : 期待値データセットの設定（オプション） - [データソース設定](../options/01-data-source.md)
* result.* : 結果出力オプション - [出力設定](../options/02-output.md)

## タイプ別オプション
### targetType

#### data: データセット比較モード
* setting: テーブル比較設定ファイル
* settingEncoding: 設定ファイルのエンコーディング

#### image: 画像比較モード
* image.*: [画像比較オプション](../options/04-image.md)
* new/old.srcType: 自動的に'file'に設定
* new/old.extension: 自動的に'png'に設定

#### pdf: PDF比較モード
* image.*: [画像比較オプション](../options/04-image.md)（PDFを画像として比較）
* new/old.srcType: 自動的に'file'に設定
* new/old.extension: 自動的に'pdf'に設定

## 使用例
```bash
# データセット比較
dbunit compare -targetType data \
  -new.srcType csv -new.src new_data.csv \
  -old.srcType csv -old.src old_data.csv \
  -result.resultType csv -result.resultDir ./diff

# 画像比較
dbunit compare -targetType image \
  -new.src new_image.png \
  -old.src old_image.png \
  -image.threshold 10 \
  -image.pixelToleranceLevel 0.05 \
  -result.resultDir ./diff