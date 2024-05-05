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
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageCompareOption implements OptionParser<ImageCompareDto> {

    private static final Pattern AREA_REGEX = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");
    private final String prefix;
    private final List<Rectangle> excludeAreaList = new ArrayList<>();
    private boolean drawExcludedRectangles = true;
    private String threshold;
    private String pixelToleranceLevel;
    private String allowingPercentOfDifferentPixels;
    private String rectangleLineWidth;
    private String minimalRectangleSize;
    private String maximalRectangleCount;
    private boolean fillDifferenceRectangles;
    private String percentOpacityDifferenceRectangles;
    private String differenceRectangleColor;
    private String excludedAreas;
    private boolean fillExcludedRectangles;
    private String percentOpacityExcludedRectangles;
    private String excludedRectangleColor;

    public ImageCompareOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-threshold", this.threshold);
        result.put("-pixelToleranceLevel", this.pixelToleranceLevel);
        result.put("-allowingPercentOfDifferentPixels", this.allowingPercentOfDifferentPixels);
        result.put("-rectangleLineWidth", this.rectangleLineWidth);
        result.put("-minimalRectangleSize", this.minimalRectangleSize);
        result.put("-maximalRectangleCount", this.maximalRectangleCount);
        result.put("-excludedAreas", this.excludedAreas);
        result.put("-drawExcludedRectangles", String.valueOf(this.drawExcludedRectangles));
        result.put("-fillExcludedRectangles", String.valueOf(this.fillExcludedRectangles));
        result.put("-percentOpacityExcludedRectangles", this.percentOpacityExcludedRectangles);
        result.put("-excludedRectangleColor", this.excludedRectangleColor);
        result.put("-fillDifferenceRectangles", String.valueOf(this.fillDifferenceRectangles));
        result.put("-percentOpacityDifferenceRectangles", this.percentOpacityDifferenceRectangles);
        result.put("-differenceRectangleColor", this.differenceRectangleColor);
        return result;
    }

    @Override
    public void setUpComponent(final ImageCompareDto dto) {
        if (Strings.isNotEmpty(dto.getDrawExcludedRectangles())) {
            this.drawExcludedRectangles = Boolean.parseBoolean(dto.getDrawExcludedRectangles());
        }
        this.threshold = dto.getThreshold();
        this.pixelToleranceLevel = dto.getPixelToleranceLevel();
        this.allowingPercentOfDifferentPixels = dto.getAllowingPercentOfDifferentPixels();
        this.rectangleLineWidth = dto.getRectangleLineWidth();
        this.minimalRectangleSize = dto.getMinimalRectangleSize();
        this.maximalRectangleCount = dto.getMaximalRectangleCount();
        if (Strings.isNotEmpty(dto.getFillDifferenceRectangles())) {
            this.fillDifferenceRectangles = Boolean.parseBoolean(dto.getFillDifferenceRectangles());
        }
        this.percentOpacityDifferenceRectangles = dto.getPercentOpacityDifferenceRectangles();
        this.differenceRectangleColor = dto.getDifferenceRectangleColor();
        this.excludedAreas = dto.getExcludedAreas();
        if (Strings.isNotEmpty(dto.getFillExcludedRectangles())) {
            this.fillExcludedRectangles = Boolean.parseBoolean(dto.getFillExcludedRectangles());
        }
        this.percentOpacityExcludedRectangles = dto.getPercentOpacityExcludedRectangles();
        this.excludedRectangleColor = dto.getExcludedRectangleColor();
        if (!Optional.ofNullable(this.excludedAreas).orElse("").isEmpty()) {
            final Matcher m = ImageCompareOption.AREA_REGEX.matcher(this.excludedAreas);
            while (m.find()) {
                final String[] points = m.group(1).split(",");
                this.excludeAreaList.add(new Rectangle(new BigDecimal(points[0]).intValue()
                        , new BigDecimal(points[1]).intValue()
                        , new BigDecimal(points[2]).intValue()
                        , new BigDecimal(points[3]).intValue()
                ));
            }
        }
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
                .setExcludeAreaList(this.excludeAreaList)
                .setDrawExcludedRectangles(this.drawExcludedRectangles)
                .setFillExcludedRectangles(this.fillExcludedRectangles)
                .setPercentOpacityExcludedRectangles(Double.parseDouble(Optional.ofNullable(this.percentOpacityExcludedRectangles).orElse("20.0")))
                .setExcludedRectangleColor(Optional.ofNullable(Color.getColor(this.excludedRectangleColor)).orElse(Color.green))
                ;
    }

}
