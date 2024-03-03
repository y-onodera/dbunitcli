package yo.dbunitcli.application.argument;

import com.github.romankh3.image.comparison.model.Rectangle;
import picocli.CommandLine;
import yo.dbunitcli.application.CompareOption;
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

public class ImageCompareOption extends DefaultArgumentsParser {

    private static final Pattern AREA_REGEX = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");
    private final List<Rectangle> excludeAreaList = new ArrayList<>();
    @CommandLine.Option(names = "-drawExcludedRectangles", defaultValue = "true", description = "Flag which says draw excluded rectangles or not.")
    private boolean drawExcludedRectangles;
    @CommandLine.Option(names = "-threshold", description = "the max distance between non-equal pixels.")
    private String threshold;
    @CommandLine.Option(names = "-pixelToleranceLevel", description = "Level of the pixel tolerance. By default it's 0.1 -> 10% difference.")
    private String pixelToleranceLevel;
    @CommandLine.Option(names = "-allowingPercentOfDifferentPixels", description = "The percent of the allowing pixels to be different to stay MATCH for comparison.")
    private String allowingPercentOfDifferentPixels;
    @CommandLine.Option(names = "-rectangleLineWidth", description = "Width of the line that is drawn the rectangle.")
    private String rectangleLineWidth;
    @CommandLine.Option(names = "-minimalRectangleSize", description = "The number of the minimal rectangle size.")
    private String minimalRectangleSize;
    @CommandLine.Option(names = "-maximalRectangleCount", description = "Maximal count of the Rectangles, which would be drawn.")
    private String maximalRectangleCount;
    @CommandLine.Option(names = "-fillDifferenceRectangles", description = "Flag which says fill difference rectangles or not.")
    private boolean fillDifferenceRectangles;
    @CommandLine.Option(names = "-percentOpacityDifferenceRectangles", description = "The desired opacity of the difference rectangle fill.")
    private String percentOpacityDifferenceRectangles;
    @CommandLine.Option(names = "-differenceRectangleColor", description = "Rectangle color of image difference.")
    private String differenceRectangleColor;
    @CommandLine.Option(names = "-excludedAreas", description = "ExcludedAreas contains a List of Rectangles to be ignored.")
    private String excludedAreas;
    @CommandLine.Option(names = "-fillExcludedRectangles", description = "Flag which says fill excluded rectangles or not.")
    private boolean fillExcludedRectangles;

    @CommandLine.Option(names = "-percentOpacityExcludedRectangles", description = "The desired opacity of the excluded rectangle fill..")
    private String percentOpacityExcludedRectangles;

    @CommandLine.Option(names = "-excludedRectangleColor", description = "Rectangle color of excluded part..")
    private String excludedRectangleColor;

    public ImageCompareOption(final String prefix) {
        super(prefix);
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
    public void setUpComponent(final String[] expandArgs) {
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
