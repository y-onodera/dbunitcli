package yo.dbunitcli.application.option;

import com.github.romankh3.image.comparison.model.Rectangle;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.CompareOption;
import yo.dbunitcli.application.dto.ImageCompareDto;
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
        , boolean drawExcludedRectangles
        , String threshold
        , String pixelToleranceLevel
        , String allowingPercentOfDifferentPixels
        , String rectangleLineWidth
        , String minimalRectangleSize
        , String maximalRectangleCount
        , boolean fillDifferenceRectangles
        , String percentOpacityDifferenceRectangles
        , String differenceRectangleColor
        , String excludedAreas
        , boolean fillExcludedRectangles
        , String percentOpacityExcludedRectangles
        , String excludedRectangleColor
) implements Option {

    private static final Pattern AREA_REGEX = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");

    public ImageCompareOption(final String prefix) {
        this(prefix, new ImageCompareDto());
    }

    public ImageCompareOption(final String prefix, final ImageCompareDto dto) {
        this(prefix
                , !Strings.isNotEmpty(dto.getDrawExcludedRectangles()) || Boolean.parseBoolean(dto.getDrawExcludedRectangles())
                , dto.getThreshold()
                , dto.getPixelToleranceLevel()
                , dto.getAllowingPercentOfDifferentPixels()
                , dto.getRectangleLineWidth()
                , dto.getMinimalRectangleSize()
                , dto.getMaximalRectangleCount()
                , Strings.isNotEmpty(dto.getFillDifferenceRectangles()) && Boolean.parseBoolean(dto.getFillDifferenceRectangles())
                , dto.getPercentOpacityDifferenceRectangles()
                , dto.getDifferenceRectangleColor()
                , dto.getExcludedAreas()
                , Strings.isNotEmpty(dto.getFillExcludedRectangles()) && Boolean.parseBoolean(dto.getFillExcludedRectangles())
                , dto.getPercentOpacityExcludedRectangles()
                , dto.getExcludedRectangleColor()
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-threshold", this.threshold)
                .put("-pixelToleranceLevel", this.pixelToleranceLevel)
                .put("-allowingPercentOfDifferentPixels", this.allowingPercentOfDifferentPixels)
                .put("-rectangleLineWidth", this.rectangleLineWidth)
                .put("-minimalRectangleSize", this.minimalRectangleSize)
                .put("-maximalRectangleCount", this.maximalRectangleCount)
                .put("-excludedAreas", this.excludedAreas)
                .put("-drawExcludedRectangles", this.drawExcludedRectangles)
                .put("-fillExcludedRectangles", this.fillExcludedRectangles)
                .put("-percentOpacityExcludedRectangles", this.percentOpacityExcludedRectangles)
                .put("-excludedRectangleColor", this.excludedRectangleColor)
                .put("-fillDifferenceRectangles", this.fillDifferenceRectangles)
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
                .setFillDifferenceRectangles(this.fillDifferenceRectangles)
                .setPercentOpacityDifferenceRectangles(Double.parseDouble(Optional.ofNullable(this.percentOpacityDifferenceRectangles).orElse("20.0")))
                .setDifferenceRectangleColor(Optional.ofNullable(Color.getColor(this.differenceRectangleColor)).orElse(Color.red))
                .setExcludeAreaList(this.excludeAreaList())
                .setDrawExcludedRectangles(this.drawExcludedRectangles)
                .setFillExcludedRectangles(this.fillExcludedRectangles)
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
