# 画像比較設定

画像やPDFの比較時に使用する詳細な設定です。差分の検出方法や視覚的な表示方法を制御します。

## ImageCompareOption (image.*)

### 比較の基本設定
* -threshold: 画像比較の閾値（デフォルト: 5）
  - 値が大きいほど差分判定が緩くなります
  - 推奨範囲: 1-20
* -pixelToleranceLevel: ピクセル許容レベル（デフォルト: 0.01）
  - 色の違いをどの程度許容するかを指定
  - 0.0-1.0の範囲で指定（0.0が完全一致）
* -allowingPercentOfDifferentPixels: 許容される異なるピクセルの割合（デフォルト: 0.01）
  - 画像全体の何%まで差分を許容するか
  - 0.0-100.0の範囲で指定

### 差分表示の設定
* -rectangleLineWidth: 差分表示用の四角形の線幅（デフォルト: 1）
* -minimalRectangleSize: 差分として検出する最小の四角形サイズ（デフォルト: 1）
* -maximalRectangleCount: 差分として検出する最大の四角形数（デフォルト: -1, 無制限）
* -fillDifferenceRectangles: 差分を示す四角形を塗りつぶすかどうか（デフォルト: false）
* -percentOpacityDifferenceRectangles: 差分四角形の透明度（デフォルト: 20.0）
* -differenceRectangleColor: 差分四角形の色（デフォルト: red）

### 除外領域の設定
* -excludedAreas: 比較から除外する領域（書式: [x,y,width,height][x,y,width,height]...）
* -drawExcludedRectangles: 除外領域を描画するかどうか（デフォルト: true）
* -fillExcludedRectangles: 除外領域を塗りつぶすかどうか（デフォルト: false）
* -percentOpacityExcludedRectangles: 除外領域の透明度（デフォルト: 20.0）
* -excludedRectangleColor: 除外領域の色（デフォルト: green）

## 使用例
```bash
# 高精度な画像比較
dbunit compare -targetType image \
  -new.src new_image.png \
  -old.src old_image.png \
  -image.threshold 3 \
  -image.pixelToleranceLevel 0.001 \
  -image.allowingPercentOfDifferentPixels 0.0

# 特定領域を除外した比較
dbunit compare -targetType image \
  -new.src new_image.png \
  -old.src old_image.png \
  -image.excludedAreas "[0,0,100,50][200,300,50,50]" \
  -image.fillExcludedRectangles true \
  -image.percentOpacityExcludedRectangles 30.0