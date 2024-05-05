package yo.dbunitcli.application.dto;

import picocli.CommandLine;

public class ImageCompareDto {
    @CommandLine.Option(names = "-drawExcludedRectangles", defaultValue = "true", description = "Flag which says draw excluded rectangles or not.")
    private String drawExcludedRectangles;
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
    private String fillDifferenceRectangles;
    @CommandLine.Option(names = "-percentOpacityDifferenceRectangles", description = "The desired opacity of the difference rectangle fill.")
    private String percentOpacityDifferenceRectangles;
    @CommandLine.Option(names = "-differenceRectangleColor", description = "Rectangle color of image difference.")
    private String differenceRectangleColor;
    @CommandLine.Option(names = "-excludedAreas", description = "ExcludedAreas contains a List of Rectangles to be ignored.")
    private String excludedAreas;
    @CommandLine.Option(names = "-fillExcludedRectangles", description = "Flag which says fill excluded rectangles or not.")
    private String fillExcludedRectangles;

    @CommandLine.Option(names = "-percentOpacityExcludedRectangles", description = "The desired opacity of the excluded rectangle fill..")
    private String percentOpacityExcludedRectangles;

    @CommandLine.Option(names = "-excludedRectangleColor", description = "Rectangle color of excluded part..")
    private String excludedRectangleColor;

    public String getDrawExcludedRectangles() {
        return this.drawExcludedRectangles;
    }

    public void setDrawExcludedRectangles(final String drawExcludedRectangles) {
        this.drawExcludedRectangles = drawExcludedRectangles;
    }

    public String getThreshold() {
        return this.threshold;
    }

    public void setThreshold(final String threshold) {
        this.threshold = threshold;
    }

    public String getPixelToleranceLevel() {
        return this.pixelToleranceLevel;
    }

    public void setPixelToleranceLevel(final String pixelToleranceLevel) {
        this.pixelToleranceLevel = pixelToleranceLevel;
    }

    public String getAllowingPercentOfDifferentPixels() {
        return this.allowingPercentOfDifferentPixels;
    }

    public void setAllowingPercentOfDifferentPixels(final String allowingPercentOfDifferentPixels) {
        this.allowingPercentOfDifferentPixels = allowingPercentOfDifferentPixels;
    }

    public String getRectangleLineWidth() {
        return this.rectangleLineWidth;
    }

    public void setRectangleLineWidth(final String rectangleLineWidth) {
        this.rectangleLineWidth = rectangleLineWidth;
    }

    public String getMinimalRectangleSize() {
        return this.minimalRectangleSize;
    }

    public void setMinimalRectangleSize(final String minimalRectangleSize) {
        this.minimalRectangleSize = minimalRectangleSize;
    }

    public String getMaximalRectangleCount() {
        return this.maximalRectangleCount;
    }

    public void setMaximalRectangleCount(final String maximalRectangleCount) {
        this.maximalRectangleCount = maximalRectangleCount;
    }

    public String getFillDifferenceRectangles() {
        return this.fillDifferenceRectangles;
    }

    public void setFillDifferenceRectangles(final String fillDifferenceRectangles) {
        this.fillDifferenceRectangles = fillDifferenceRectangles;
    }

    public String getPercentOpacityDifferenceRectangles() {
        return this.percentOpacityDifferenceRectangles;
    }

    public void setPercentOpacityDifferenceRectangles(final String percentOpacityDifferenceRectangles) {
        this.percentOpacityDifferenceRectangles = percentOpacityDifferenceRectangles;
    }

    public String getDifferenceRectangleColor() {
        return this.differenceRectangleColor;
    }

    public void setDifferenceRectangleColor(final String differenceRectangleColor) {
        this.differenceRectangleColor = differenceRectangleColor;
    }

    public String getExcludedAreas() {
        return this.excludedAreas;
    }

    public void setExcludedAreas(final String excludedAreas) {
        this.excludedAreas = excludedAreas;
    }

    public String getFillExcludedRectangles() {
        return this.fillExcludedRectangles;
    }

    public void setFillExcludedRectangles(final String fillExcludedRectangles) {
        this.fillExcludedRectangles = fillExcludedRectangles;
    }

    public String getPercentOpacityExcludedRectangles() {
        return this.percentOpacityExcludedRectangles;
    }

    public void setPercentOpacityExcludedRectangles(final String percentOpacityExcludedRectangles) {
        this.percentOpacityExcludedRectangles = percentOpacityExcludedRectangles;
    }

    public String getExcludedRectangleColor() {
        return this.excludedRectangleColor;
    }

    public void setExcludedRectangleColor(final String excludedRectangleColor) {
        this.excludedRectangleColor = excludedRectangleColor;
    }
}
