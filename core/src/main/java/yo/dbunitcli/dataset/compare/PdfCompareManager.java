package yo.dbunitcli.dataset.compare;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
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

    public PdfCompareManager(final ImageCompareBuilder builder) {
        super(builder);
    }

    @Override
    protected RowCompareResultHandler getRowResultHandler(final TableCompare it) {
        return new PdfFileCompareHandler(it);
    }

    public class PdfFileCompareHandler extends ImageFileCompareHandler {

        protected List<CompareDiff> pageDiffs;

        protected PdfFileCompareHandler(final TableCompare it) {
            super(it);
            this.pageDiffs = new ArrayList<>();
        }

        @Override
        public List<CompareDiff> result() {
            final List<CompareDiff> results = super.result();
            results.addAll(this.pageDiffs);
            return results;
        }

        @Override
        protected void compareFile(final File oldPath, final File newPath, final CompareKeys key) {
            try (final InputStream newIn = new FileInputStream(newPath); final InputStream oldIn = new FileInputStream(oldPath)) {
                try (final PDDocument doc = PDDocument.load(newIn); final PDDocument oldDoc = PDDocument.load(oldIn)) {
                    final int newPages = doc.getNumberOfPages();
                    final int oldPages = oldDoc.getNumberOfPages();
                    if (newPages != oldPages) {
                        this.pageDiffs.add(((CompareDiff.Diff) () -> String.format("Page Number Change[new:%d,old:%d]"
                                , newPages
                                , oldPages)).of()
                                .setTargetName(oldPath.getName())
                                .build());
                    } else {
                        IntStream.range(0, oldPages).forEach(i -> {
                            final String page = String.format("%0" + String.valueOf(oldPages).length() + "d", i + 1);
                            final ImageComparisonResult result = this.compareImage(this.getImage(doc, i), this.getImage(oldDoc, i));
                            if (result.getImageComparisonState() != ImageComparisonState.MATCH) {
                                result.writeResultTo(new File(this.resultDir, key.getKeysToString() + page + ".png"));
                                this.modifyValues.add(this.getDiff(result)
                                        .of()
                                        .setTargetName(oldPath.getName() + " page" + page)
                                        .build());
                            }

                        });
                    }
                }
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }

        private BufferedImage getImage(final PDDocument doc, final int i) {
            try {
                return new PDFRenderer(doc).renderImage(i);
            } catch (final IOException e) {
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