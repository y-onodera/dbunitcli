package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ImageCompareManager extends DefaultCompareManager {

    protected final int threshold;

    protected final double allowingPercentOfDifferentPixels;

    protected final double pixelToleranceLevel;

    protected final int rectangleLineWidth;

    protected final Integer minimalRectangleSize;

    protected final Integer maximalRectangleCount;

    protected final boolean fillDifferenceRectangles;

    protected final double percentOpacityDifferenceRectangles;

    protected final Color differenceRectangleColor;

    protected final List<Rectangle> excludeAreaList;

    protected final boolean drawExcludedRectangles;

    protected final Color excludedRectangleColor;

    protected final boolean fillExcludedRectangles;

    protected final double percentOpacityExcludedRectangles;

    public ImageCompareManager(ImageCompareBuilder builder) {
        this.threshold = builder.getThreshold();
        this.allowingPercentOfDifferentPixels = builder.getAllowingPercentOfDifferentPixels();
        this.pixelToleranceLevel = builder.getPixelToleranceLevel();
        this.rectangleLineWidth = builder.getRectangleLineWidth();
        this.minimalRectangleSize = builder.getMinimalRectangleSize();
        this.maximalRectangleCount = builder.getMaximalRectangleCount();
        this.fillDifferenceRectangles = builder.isFillDifferenceRectangles();
        this.percentOpacityDifferenceRectangles = builder.getPercentOpacityDifferenceRectangles();
        this.differenceRectangleColor = builder.getDifferenceRectangleColor();
        this.excludeAreaList = builder.getExcludeAreaList();
        this.drawExcludedRectangles = builder.isDrawExcludedRectangles();
        this.excludedRectangleColor = builder.getExcludedRectangleColor();
        this.fillExcludedRectangles = builder.isFillExcludedRectangles();
        this.percentOpacityExcludedRectangles = builder.getPercentOpacityExcludedRectangles();
    }

    @Override
    public CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results) {
        return new ImageCompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
    }

    @Override
    public Stream<Function<Compare, List<CompareDiff>>> getStrategies() {
        return Stream.of(this.searchModifyTables());
    }

    @Override
    public List<CompareDiff> compareTable(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
        return new ImageMain(oldTable, newTable, comparisonKeys, writer).getResults();
    }

    public class ImageMain extends Main {

        protected final File resultDir;

        protected int diffCount;

        public ImageMain(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
            super(oldTable, newTable, comparisonKeys, writer);
            this.resultDir = writer.getDir();
        }

        @Override
        protected List<CompareDiff> compareColumn() {
            return Lists.newArrayList();
        }

        @Override
        protected void compareKey(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Object[] oldRow, CompareKeys key) throws DataSetException {
            this.compareFile(new File(oldRow[0].toString()), new File(newRowLists.get(key).getValue()[0].toString()), key);
        }

        protected void compareFile(File oldPath, File newPath, CompareKeys key) throws DataSetException {
            try {
                BufferedImage newImage = this.toImage(newPath);
                BufferedImage oldImage = this.toImage(oldPath);
                ImageComparisonResult result = this.compareImage(key, newImage, oldImage);
                if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                    result.writeResultTo(new File(resultDir, key.getKeysToString() + ".png"));
                    this.modifyValues.put(this.diffCount++, ((CompareDiff.Diff) () -> result.getRectangles()
                            .stream().reduce("", (String sb, Rectangle it) -> sb + String.format("[%s,%s,%s,%s]"
                                    , it.getMinPoint().getX(), it.getMinPoint().getY()
                                    , it.getMaxPoint().getX(), it.getMaxPoint().getY()), (a, b) -> a + b)
                    ).of().setTargetName(oldPath.getName()).build());
                }
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }

        protected BufferedImage toImage(File newPath) throws IOException {
            return ImageIO.read(newPath);
        }

        protected ImageComparisonResult compareImage(CompareKeys key, BufferedImage newImage, BufferedImage oldImage) throws DataSetException {
            return new ImageComparison(oldImage, newImage)
                    .setThreshold(threshold)
                    .setAllowingPercentOfDifferentPixels(allowingPercentOfDifferentPixels)
                    .setPixelToleranceLevel(pixelToleranceLevel)
                    .setRectangleLineWidth(rectangleLineWidth)
                    .setMinimalRectangleSize(minimalRectangleSize)
                    .setMaximalRectangleCount(maximalRectangleCount)
                    .setDifferenceRectangleFilling(fillDifferenceRectangles, percentOpacityDifferenceRectangles)
                    .setDifferenceRectangleColor(differenceRectangleColor)
                    .setExcludedAreas(excludeAreaList)
                    .setDrawExcludedRectangles(drawExcludedRectangles && excludeAreaList.size() > 0)
                    .setExcludedRectangleColor(excludedRectangleColor)
                    .setExcludedRectangleFilling(fillExcludedRectangles, percentOpacityExcludedRectangles)
                    .compareImages();
        }
    }
}