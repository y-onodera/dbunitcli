package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class ImageCompareBuilder implements Supplier<DataSetCompare.Manager> {

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
    public DataSetCompare.Manager get() {
        return new ImageCompareManager(this);
    }

    public int getThreshold() {
        return this.threshold;
    }

    public ImageCompareBuilder setThreshold(final int threshold) {
        this.threshold = threshold;
        return this;
    }

    public int getRectangleLineWidth() {
        return this.rectangleLineWidth;
    }

    public ImageCompareBuilder setRectangleLineWidth(final int rectangleLineWidth) {
        this.rectangleLineWidth = rectangleLineWidth;
        return this;
    }

    public Integer getMinimalRectangleSize() {
        return this.minimalRectangleSize;
    }

    public ImageCompareBuilder setMinimalRectangleSize(final Integer minimalRectangleSize) {
        this.minimalRectangleSize = minimalRectangleSize;
        return this;
    }

    public Integer getMaximalRectangleCount() {
        return this.maximalRectangleCount;
    }

    public ImageCompareBuilder setMaximalRectangleCount(final Integer maximalRectangleCount) {
        this.maximalRectangleCount = maximalRectangleCount;
        return this;
    }

    public double getPixelToleranceLevel() {
        return this.pixelToleranceLevel;
    }

    public ImageCompareBuilder setPixelToleranceLevel(final double pixelToleranceLevel) {
        this.pixelToleranceLevel = pixelToleranceLevel;
        return this;
    }

    public List<Rectangle> getExcludeAreaList() {
        return this.excludeAreaList;
    }

    public ImageCompareBuilder setExcludeAreaList(final List<Rectangle> excludeAreaList) {
        this.excludeAreaList = excludeAreaList;
        return this;
    }

    public boolean isDrawExcludedRectangles() {
        return this.drawExcludedRectangles;
    }

    public ImageCompareBuilder setDrawExcludedRectangles(final boolean drawExcludedRectangles) {
        this.drawExcludedRectangles = drawExcludedRectangles;
        return this;
    }

    public boolean isFillExcludedRectangles() {
        return this.fillExcludedRectangles;
    }

    public ImageCompareBuilder setFillExcludedRectangles(final boolean fillExcludedRectangles) {
        this.fillExcludedRectangles = fillExcludedRectangles;
        return this;
    }

    public double getPercentOpacityExcludedRectangles() {
        return this.percentOpacityExcludedRectangles;
    }

    public ImageCompareBuilder setPercentOpacityExcludedRectangles(final double percentOpacityExcludedRectangles) {
        this.percentOpacityExcludedRectangles = percentOpacityExcludedRectangles;
        return this;
    }

    public boolean isFillDifferenceRectangles() {
        return this.fillDifferenceRectangles;
    }

    public ImageCompareBuilder setFillDifferenceRectangles(final boolean fillDifferenceRectangles) {
        this.fillDifferenceRectangles = fillDifferenceRectangles;
        return this;
    }

    public double getPercentOpacityDifferenceRectangles() {
        return this.percentOpacityDifferenceRectangles;
    }

    public ImageCompareBuilder setPercentOpacityDifferenceRectangles(final double percentOpacityDifferenceRectangles) {
        this.percentOpacityDifferenceRectangles = percentOpacityDifferenceRectangles;
        return this;
    }

    public double getAllowingPercentOfDifferentPixels() {
        return this.allowingPercentOfDifferentPixels;
    }

    public ImageCompareBuilder setAllowingPercentOfDifferentPixels(final double allowingPercentOfDifferentPixels) {
        this.allowingPercentOfDifferentPixels = allowingPercentOfDifferentPixels;
        return this;
    }

    public Color getDifferenceRectangleColor() {
        return this.differenceRectangleColor;
    }

    public ImageCompareBuilder setDifferenceRectangleColor(final Color differenceRectangleColor) {
        this.differenceRectangleColor = differenceRectangleColor;
        return this;
    }

    public Color getExcludedRectangleColor() {
        return this.excludedRectangleColor;
    }

    public ImageCompareBuilder setExcludedRectangleColor(final Color excludedRectangleColor) {
        this.excludedRectangleColor = excludedRectangleColor;
        return this;
    }

}
