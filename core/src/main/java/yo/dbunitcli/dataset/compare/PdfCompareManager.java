package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.google.common.base.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.CompareKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

public class PdfCompareManager extends ImageCompareManager {

    public static Builder builder() {
        return new Builder();
    }

    public PdfCompareManager(ImageCompareBuilder builder) {
        super(builder);
    }

    @Override
    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> compareRow() {
        return it -> {
            try {
                return new PdfFileCompare(it).exec();
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    public class PdfFileCompare extends ImageFileCompare {

        public PdfFileCompare(DataSetCompare.TableCompare it) throws DataSetException {
            super(it);
        }

        @Override
        protected void compareFile(File oldPath, File newPath, CompareKeys key) throws DataSetException {
            try (InputStream newIn = new FileInputStream(newPath); InputStream oldIn = new FileInputStream(oldPath)) {
                try (PDDocument doc = PDDocument.load(newIn); PDDocument oldDoc = PDDocument.load(oldIn)) {
                    for (int i = 0, j = oldDoc.getNumberOfPages(); i < j; i++) {
                        String page = Strings.padStart(String.valueOf(i), String.valueOf(j).length(), '0');
                        ImageComparisonResult result = this.compareImage(new PDFRenderer(doc).renderImage(i), new PDFRenderer(oldDoc).renderImage(i));
                        if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                            result.writeResultTo(new File(this.resultDir, key.getKeysToString() + page + ".png"));
                            this.modifyValues.put(this.diffCount++, this.getDiff(result)
                                    .of()
                                    .setTargetName(oldPath.getName() + "_" + page)
                                    .build());
                        }

                    }
                }
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }
    }

    public static class Builder extends ImageCompareBuilder {
        @Override
        public DefaultCompareManager get() {
            return new PdfCompareManager(this);
        }
    }

}