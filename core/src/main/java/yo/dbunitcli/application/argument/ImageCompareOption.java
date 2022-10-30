package yo.dbunitcli.application.argument;

import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;
import yo.dbunitcli.application.CompareOption;
import yo.dbunitcli.dataset.compare.ImageCompareBuilder;
import yo.dbunitcli.dataset.compare.PdfCompareManager;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageCompareOption extends DefaultArgumentsParser {

    private static final Pattern AREA_REGEX = Pattern.compile("(?=\\[)\\[((\\d+\\.?\\d*,){3}(\\d+\\.?\\d*))\\]((?=\\[)|$)");

    @Option(name = "-threshold", usage = "the max distance between non-equal pixels.")
    private String threshold;

    @Option(name = "-pixelToleranceLevel", usage = "Level of the pixel tolerance. By default it's 0.1 -> 10% difference.")
    private String pixelToleranceLevel;

    @Option(name = "-allowingPercentOfDifferentPixels", usage = "The percent of the allowing pixels to be different to stay MATCH for comparison.")
    private String allowingPercentOfDifferentPixels;

    @Option(name = "-rectangleLineWidth", usage = "Width of the line that is drawn the rectangle.")
    private String rectangleLineWidth;

    @Option(name = "-minimalRectangleSize", usage = "The number of the minimal rectangle size.")
    private String minimalRectangleSize;

    @Option(name = "-maximalRectangleCount", usage = "Maximal count of the Rectangles, which would be drawn.")
    private String maximalRectangleCount;

    @Option(name = "-fillDifferenceRectangles", handler = ExplicitBooleanOptionHandler.class, usage = "Flag which says fill difference rectangles or not.")
    private boolean fillDifferenceRectangles;

    @Option(name = "-percentOpacityDifferenceRectangles", usage = "The desired opacity of the difference rectangle fill.")
    private String percentOpacityDifferenceRectangles;

    @Option(name = "-differenceRectangleColor", usage = "Rectangle color of image difference.")
    private String differenceRectangleColor;

    @Option(name = "-excludedAreas", usage = "ExcludedAreas contains a List of Rectangles to be ignored.")
    private String excludedAreas;

    private final List<Rectangle> excludeAreaList = Lists.newArrayList();

    @Option(name = "-drawExcludedRectangles", handler = ExplicitBooleanOptionHandler.class, usage = "Flag which says draw excluded rectangles or not.")
    private boolean drawExcludedRectangles = true;

    @Option(name = "-fillExcludedRectangles", handler = ExplicitBooleanOptionHandler.class, usage = "Flag which says fill excluded rectangles or not.")
    private boolean fillExcludedRectangles;

    @Option(name = "-percentOpacityExcludedRectangles", usage = "The desired opacity of the excluded rectangle fill..")
    private String percentOpacityExcludedRectangles;

    @Option(name = "-excludedRectangleColor", usage = "Rectangle color of excluded part..")
    private String excludedRectangleColor;

    public ImageCompareOption(String prefix) {
        super(prefix);
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        if (!Strings.isNullOrEmpty(this.excludedAreas)) {
            Matcher m = AREA_REGEX.matcher(this.excludedAreas);
            while (m.find()) {
                String[] points = m.group(1).split(",");
                excludeAreaList.add(new Rectangle(new BigDecimal(points[0]).intValue()
                        , new BigDecimal(points[1]).intValue()
                        , new BigDecimal(points[2]).intValue()
                        , new BigDecimal(points[3]).intValue()
                ));
            }
        }
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
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

    public ImageCompareBuilder createFactoryOf(CompareOption.Type targetType) {
        ImageCompareBuilder result = targetType == CompareOption.Type.image ? new ImageCompareBuilder() : PdfCompareManager.builder();
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
