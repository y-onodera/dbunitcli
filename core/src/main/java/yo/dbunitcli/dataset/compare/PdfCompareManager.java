package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.google.common.base.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import yo.dbunitcli.dataset.CompareKeys;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

        protected List<CompareDiff> pageDiffs;

        protected PdfFileCompareHandler(DataSetCompare.TableCompare it) {
            super(it);
            this.pageDiffs = new ArrayList<>();
        }

        @Override
        public List<CompareDiff> result() {
            List<CompareDiff> results = super.result();
            results.addAll(pageDiffs);
            return results;
        }

        @Override
        protected void compareFile(File oldPath, File newPath, CompareKeys key) {
            try (InputStream newIn = new FileInputStream(newPath); InputStream oldIn = new FileInputStream(oldPath)) {
                try (PDDocument doc = PDDocument.load(newIn); PDDocument oldDoc = PDDocument.load(oldIn)) {
                    if (doc.getNumberOfPages() != oldDoc.getNumberOfPages()) {
                        pageDiffs.add(((CompareDiff.Diff) () -> String.format("Page Number Change[new:%d,old:%d]"
                                , doc.getNumberOfPages()
                                , oldDoc.getNumberOfPages())).of()
                                .setTargetName(oldPath.getName())
                                .build());
                    }
                    IntStream.range(0, oldDoc.getNumberOfPages()).forEach(i -> {
                        String page = Strings.padStart(String.valueOf(i + 1), String.valueOf(oldDoc.getNumberOfPages()).length(), '0');
                        ImageComparisonResult result = this.compareImage(getImage(doc, i), getImage(oldDoc, i));
                        if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                            result.writeResultTo(new File(this.resultDir, key.getKeysToString() + page + ".png"));
                            this.modifyValues.add(this.getDiff(result)
                                    .of()
                                    .setTargetName(oldPath.getName() + " page" + page)
                                    .build());
                        }

                    });
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        private BufferedImage getImage(PDDocument doc, int i) {
            try {
                return new PDFRenderer(doc).renderImage(i);
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