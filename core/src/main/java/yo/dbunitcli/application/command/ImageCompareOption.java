package yo.dbunitcli.application.command;

import com.github.romankh3.image.comparison.model.Rectangle;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.ImageCompareDto;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.dataset.compare.ImageCompareBuilder;
import yo.dbunitcli.dataset.compare.PdfCompareManager;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ImageCompareOption(
        String prefix
        , String drawExcludedRectangles
        , String threshold
        , String pixelToleranceLevel
        , String allowingPercentOfDifferentPixels
        , String rectangleLineWidth
        , String minimalRectangleSize
        , String maximalRectangleCount
        , String fillDifferenceRectangles
        , String percentOpacityDifferenceRectangles
        , String differenceRectangleColor
        , String excludedAreas
        , String fillExcludedRectangles
        , String percentOpacityExcludedRectangles
        , String excludedRectangleColor
) implements Option {

    // excludedAreas全体の書式（[x1,y1,x2,y2]を0個以上連結）
    private static final Pattern AREAS_FORMAT = Pattern.compile("(\\[(\\d+\\.?\\d*,){3}\\d+\\.?\\d*])*");
    private static final Pattern AREA_REGEX = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");

    public ImageCompareOption(final String prefix) {
        this(prefix, new ImageCompareDto());
    }

    public ImageCompareOption(final String prefix, final ImageCompareDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getDrawExcludedRectangles()) ? dto.getDrawExcludedRectangles() : "true"
                , dto.getThreshold()
                , dto.getPixelToleranceLevel()
                , dto.getAllowingPercentOfDifferentPixels()
                , dto.getRectangleLineWidth()
                , dto.getMinimalRectangleSize()
                , dto.getMaximalRectangleCount()
                , Strings.isNotEmpty(dto.getFillDifferenceRectangles()) ? dto.getFillDifferenceRectangles() : "false"
                , dto.getPercentOpacityDifferenceRectangles()
                , dto.getDifferenceRectangleColor()
                , dto.getExcludedAreas()
                , Strings.isNotEmpty(dto.getFillExcludedRectangles()) ? dto.getFillExcludedRectangles() : "false"
                , dto.getPercentOpacityExcludedRectangles()
                , dto.getExcludedRectangleColor()
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * 書式に合わない矩形は黙って無視されるため、全体が書式どおりであることを確かめる
     */
    @Override
    public List<Violation> validate() {
        return Option.concat(Strings.isEmpty(this.excludedAreas) || AREAS_FORMAT.matcher(this.excludedAreas).matches()
                        ? List.of()
                        : this.invalidValue("-excludedAreas", "expected [x1,y1,x2,y2] repeated but was '" + this.excludedAreas + "'")
                , this.validateInteger("-threshold", this.threshold)
                , this.validateDecimal("-pixelToleranceLevel", this.pixelToleranceLevel)
                , this.validateDecimal("-allowingPercentOfDifferentPixels", this.allowingPercentOfDifferentPixels)
                , this.validateInteger("-rectangleLineWidth", this.rectangleLineWidth)
                , this.validateInteger("-minimalRectangleSize", this.minimalRectangleSize)
                , this.validateInteger("-maximalRectangleCount", this.maximalRectangleCount)
                , this.validateDecimal("-percentOpacityDifferenceRectangles", this.percentOpacityDifferenceRectangles)
                , this.validateDecimal("-percentOpacityExcludedRectangles", this.percentOpacityExcludedRectangles)
                , this.validateBoolean("-drawExcludedRectangles", this.drawExcludedRectangles)
                , this.validateBoolean("-fillDifferenceRectangles", this.fillDifferenceRectangles)
                , this.validateBoolean("-fillExcludedRectangles", this.fillExcludedRectangles));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-threshold", this.threshold)
                .put("-pixelToleranceLevel", this.pixelToleranceLevel)
                .put("-allowingPercentOfDifferentPixels", this.allowingPercentOfDifferentPixels)
                .put("-rectangleLineWidth", this.rectangleLineWidth)
                .put("-minimalRectangleSize", this.minimalRectangleSize)
                .put("-maximalRectangleCount", this.maximalRectangleCount)
                .put("-excludedAreas", this.excludedAreas)
                .putFlag("-drawExcludedRectangles", this.drawExcludedRectangles)
                .putFlag("-fillExcludedRectangles", this.fillExcludedRectangles)
                .put("-percentOpacityExcludedRectangles", this.percentOpacityExcludedRectangles)
                .put("-excludedRectangleColor", this.excludedRectangleColor)
                .putFlag("-fillDifferenceRectangles", this.fillDifferenceRectangles)
                .put("-percentOpacityDifferenceRectangles", this.percentOpacityDifferenceRectangles)
                .put("-differenceRectangleColor", this.differenceRectangleColor);
    }

    public ImageCompareBuilder createFactoryOf(final CompareOption.Type targetType) {
        final ImageCompareBuilder result = targetType == CompareOption.Type.image ? new ImageCompareBuilder() : PdfCompareManager.builder();
        return result
                .setThreshold(Integer.parseInt(Optional.ofNullable(this.threshold).orElse("5")))
                .setPixelToleranceLevel(Double.parseDouble(Optional.ofNullable(this.pixelToleranceLevel).orElse("0.01D")))
                .setAllowingPercentOfDifferentPixels(Double.parseDouble(Optional.ofNullable(this.allowingPercentOfDifferentPixels).orElse("0.01D")))
                .setRectangleLineWidth(Integer.parseInt(Optional.ofNullable(this.rectangleLineWidth).orElse("1")))
                .setMinimalRectangleSize(Integer.valueOf(Optional.ofNullable(this.minimalRectangleSize).orElse("1")))
                .setMaximalRectangleCount(Integer.valueOf(Optional.ofNullable(this.maximalRectangleCount).orElse("-1")))
                .setFillDifferenceRectangles(Boolean.parseBoolean(this.fillDifferenceRectangles))
                .setPercentOpacityDifferenceRectangles(Double.parseDouble(Optional.ofNullable(this.percentOpacityDifferenceRectangles).orElse("20.0")))
                .setDifferenceRectangleColor(Optional.ofNullable(Color.getColor(this.differenceRectangleColor)).orElse(Color.red))
                .setExcludeAreaList(this.excludeAreaList())
                .setDrawExcludedRectangles(Boolean.parseBoolean(this.drawExcludedRectangles))
                .setFillExcludedRectangles(Boolean.parseBoolean(this.fillExcludedRectangles))
                .setPercentOpacityExcludedRectangles(Double.parseDouble(Optional.ofNullable(this.percentOpacityExcludedRectangles).orElse("20.0")))
                .setExcludedRectangleColor(Optional.ofNullable(Color.getColor(this.excludedRectangleColor)).orElse(Color.green))
                ;
    }

    private List<Rectangle> excludeAreaList() {
        final ArrayList<Rectangle> excludeAreaList = new ArrayList<>();
        if (!Optional.ofNullable(this.excludedAreas).orElse("").isEmpty()) {
            final Matcher m = ImageCompareOption.AREA_REGEX.matcher(this.excludedAreas);
            while (m.find()) {
                final String[] points = m.group(1).split(",");
                excludeAreaList.add(new Rectangle(new BigDecimal(points[0]).intValue()
                        , new BigDecimal(points[1]).intValue()
                        , new BigDecimal(points[2]).intValue()
                        , new BigDecimal(points[3]).intValue()
                ));
            }
        }
        return excludeAreaList;
    }

}
