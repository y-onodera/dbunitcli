package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.google.common.base.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import yo.dbunitcli.dataset.CompareKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfCompareManager extends ImageCompareManager {

    public static Builder builder() {
        return new Builder();
    }

    public PdfCompareManager(ImageCompareBuilder builder) {
        super(builder);
    }

    @Override
    protected RowCompareResultHandler getRowResultHandler(DataSetCompare.TableCompare it) {
        return new PdfFileCompareHandler(it);
    }

    public class PdfFileCompareHandler extends ImageFileCompareHandler {

        protected PdfFileCompareHandler(DataSetCompare.TableCompare it) {
            super(it);
        }

        @Override
        protected void compareFile(File oldPath, File newPath, CompareKeys key) {
            try (InputStream newIn = new FileInputStream(newPath); InputStream oldIn = new FileInputStream(oldPath)) {
                try (PDDocument doc = PDDocument.load(newIn); PDDocument oldDoc = PDDocument.load(oldIn)) {
                    for (int i = 0, j = oldDoc.getNumberOfPages(); i < j; i++) {
                        String page = Strings.padStart(String.valueOf(i), String.valueOf(j).length(), '0');
                        ImageComparisonResult result = this.compareImage(new PDFRenderer(doc).renderImage(i), new PDFRenderer(oldDoc).renderImage(i));
                        if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                            result.writeResultTo(new File(this.resultDir, key.getKeysToString() + page + ".png"));
                            this.modifyValues.add(this.getDiff(result)
                                    .of()
                                    .setTargetName(oldPath.getName() + "_" + page)
                                    .build());
                        }

                    }
                }
            } catch (IOException e) {
                throw new AssertionError(e);
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