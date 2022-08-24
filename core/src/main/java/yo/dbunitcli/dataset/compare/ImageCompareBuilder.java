package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;

import java.awt.*;
import java.util.List;

public class ImageCompareBuilder extends DataSetCompareBuilder {

    private int threshold;

    private int rectangleLineWidth;

    private Integer minimalRectangleSize;

    private Integer maximalRectangleCount;

    private double pixelToleranceLevel;

    private List<Rectangle> excludeAreaList = Lists.newArrayList();

    private boolean drawExcludedRectangles;

    private boolean fillExcludedRectangles;

    private double percentOpacityExcludedRectangles;

    private boolean fillDifferenceRectangles;

    private double percentOpacityDifferenceRectangles;

    private double allowingPercentOfDifferentPixels;

    private Color differenceRectangleColor;

    private Color excludedRectangleColor;

    @Override
    public TableDataSetCompare getTableDataSetCompare() {
        return new ImageCompare(this);
    }

    public int getThreshold() {
        return threshold;
    }

    public ImageCompareBuilder setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public int getRectangleLineWidth() {
        return rectangleLineWidth;
    }

    public ImageCompareBuilder setRectangleLineWidth(int rectangleLineWidth) {
        this.rectangleLineWidth = rectangleLineWidth;
        return this;
    }

    public Integer getMinimalRectangleSize() {
        return minimalRectangleSize;
    }

    public ImageCompareBuilder setMinimalRectangleSize(Integer minimalRectangleSize) {
        this.minimalRectangleSize = minimalRectangleSize;
        return this;
    }

    public Integer getMaximalRectangleCount() {
        return maximalRectangleCount;
    }

    public ImageCompareBuilder setMaximalRectangleCount(Integer maximalRectangleCount) {
        this.maximalRectangleCount = maximalRectangleCount;
        return this;
    }

    public double getPixelToleranceLevel() {
        return pixelToleranceLevel;
    }

    public ImageCompareBuilder setPixelToleranceLevel(double pixelToleranceLevel) {
        this.pixelToleranceLevel = pixelToleranceLevel;
        return this;
    }

    public List<Rectangle> getExcludeAreaList() {
        return excludeAreaList;
    }

    public ImageCompareBuilder setExcludeAreaList(List<Rectangle> excludeAreaList) {
        this.excludeAreaList = excludeAreaList;
        return this;
    }

    public boolean isDrawExcludedRectangles() {
        return drawExcludedRectangles;
    }

    public ImageCompareBuilder setDrawExcludedRectangles(boolean drawExcludedRectangles) {
        this.drawExcludedRectangles = drawExcludedRectangles;
        return this;
    }

    public boolean isFillExcludedRectangles() {
        return fillExcludedRectangles;
    }

    public ImageCompareBuilder setFillExcludedRectangles(boolean fillExcludedRectangles) {
        this.fillExcludedRectangles = fillExcludedRectangles;
        return this;
    }

    public double getPercentOpacityExcludedRectangles() {
        return percentOpacityExcludedRectangles;
    }

    public ImageCompareBuilder setPercentOpacityExcludedRectangles(double percentOpacityExcludedRectangles) {
        this.percentOpacityExcludedRectangles = percentOpacityExcludedRectangles;
        return this;
    }

    public boolean isFillDifferenceRectangles() {
        return fillDifferenceRectangles;
    }

    public ImageCompareBuilder setFillDifferenceRectangles(boolean fillDifferenceRectangles) {
        this.fillDifferenceRectangles = fillDifferenceRectangles;
        return this;
    }

    public double getPercentOpacityDifferenceRectangles() {
        return percentOpacityDifferenceRectangles;
    }

    public ImageCompareBuilder setPercentOpacityDifferenceRectangles(double percentOpacityDifferenceRectangles) {
        this.percentOpacityDifferenceRectangles = percentOpacityDifferenceRectangles;
        return this;
    }

    public double getAllowingPercentOfDifferentPixels() {
        return allowingPercentOfDifferentPixels;
    }

    public ImageCompareBuilder setAllowingPercentOfDifferentPixels(double allowingPercentOfDifferentPixels) {
        this.allowingPercentOfDifferentPixels = allowingPercentOfDifferentPixels;
        return this;
    }

    public Color getDifferenceRectangleColor() {
        return differenceRectangleColor;
    }

    public ImageCompareBuilder setDifferenceRectangleColor(Color differenceRectangleColor) {
        this.differenceRectangleColor = differenceRectangleColor;
        return this;
    }

    public Color getExcludedRectangleColor() {
        return excludedRectangleColor;
    }

    public ImageCompareBuilder setExcludedRectangleColor(Color excludedRectangleColor) {
        this.excludedRectangleColor = excludedRectangleColor;
        return this;
    }

}
