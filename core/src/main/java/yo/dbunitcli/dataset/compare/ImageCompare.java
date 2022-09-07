package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.CompareKeys;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ImageCompare extends TableDataSetCompare {

    private static final Logger logger = LoggerFactory.getLogger(ImageCompare.class);

    private final int threshold;

    private final double allowingPercentOfDifferentPixels;

    private final double pixelToleranceLevel;

    private final int rectangleLineWidth;

    private final Integer minimalRectangleSize;

    private final Integer maximalRectangleCount;

    private final boolean fillDifferenceRectangles;

    private final double percentOpacityDifferenceRectangles;

    private final Color differenceRectangleColor;

    private final List<Rectangle> excludeAreaList;

    private final boolean drawExcludedRectangles;

    private final Color excludedRectangleColor;

    private final boolean fillExcludedRectangles;

    private final double percentOpacityExcludedRectangles;

    private final File resultDir;

    public ImageCompare(ImageCompareBuilder builder) {
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
        this.resultDir = builder.getResultDir();
    }

    @Override
    protected List<CompareDiff> compareColumn(ITableMetaData oldMetaData, ITableMetaData newMetaData) {
        return Lists.newArrayList();
    }

    @Override
    protected void compareKey(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Map<Integer, List<CompareKeys>> modifyValues, Object[] oldRow, CompareKeys key) throws DataSetException {
        Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
        Object[] newRow = rowEntry.getValue();
        if (!this.compareFile(new File(oldRow[0].toString()), new File(newRow[0].toString()), new File(this.resultDir, key.getKeysToString() + ".png"))) {
            modifyValues.put(0, Lists.newArrayList(key));
        }
    }

    protected boolean compareFile(File oldPath, File newPath, File destination) throws DataSetException {
        try {
            BufferedImage newImage = ImageIO.read(newPath);
            BufferedImage oldImage = ImageIO.read(oldPath);
            ImageComparisonResult result = new ImageComparison(oldImage, newImage, destination)
                    .setThreshold(this.threshold)
                    .setAllowingPercentOfDifferentPixels(this.allowingPercentOfDifferentPixels)
                    .setPixelToleranceLevel(this.pixelToleranceLevel)
                    .setRectangleLineWidth(this.rectangleLineWidth)
                    .setMinimalRectangleSize(this.minimalRectangleSize)
                    .setMaximalRectangleCount(this.maximalRectangleCount)
                    .setDifferenceRectangleFilling(this.fillDifferenceRectangles
                            , this.percentOpacityDifferenceRectangles)
                    .setDifferenceRectangleColor(this.differenceRectangleColor)
                    .setExcludedAreas(this.excludeAreaList)
                    .setDrawExcludedRectangles(this.drawExcludedRectangles && this.excludeAreaList.size() > 0)
                    .setExcludedRectangleColor(this.excludedRectangleColor)
                    .setExcludedRectangleFilling(this.fillExcludedRectangles
                            , this.percentOpacityExcludedRectangles)
                    .compareImages();
            if (result.getRectangles() != null) {
                StringBuilder sb = new StringBuilder();
                result.getRectangles()
                        .forEach(it -> sb.append(String.format("[%s,%s,%s,%s]"
                                , it.getMinPoint().getX(), it.getMinPoint().getY()
                                , it.getMaxPoint().getX(), it.getMaxPoint().getY())));
                logger.info("diff areas = " + sb);
            }
            return result.getImageComparisonState() == ImageComparisonState.MATCH;
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }
}
