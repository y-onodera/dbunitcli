package yo.dbunitcli.dataset.compare;

import com.google.common.base.Strings;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PdfCompareManager extends ImageCompareManager {

    public static Builder builder() {
        return new Builder();
    }

    public PdfCompareManager(ImageCompareBuilder builder) {
        super(builder);
    }

    @Override
    public List<CompareDiff> compareTable(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
        return new PdfMain(oldTable, newTable, comparisonKeys, writer).getResults();
    }

    public class PdfMain extends ImageMain {

        public PdfMain(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
            super(oldTable, newTable, comparisonKeys, writer);
        }

        @Override
        protected void compareFile(File oldPath, File newPath, CompareKeys key) throws DataSetException {
            try (InputStream newIn = new FileInputStream(newPath); InputStream oldIn = new FileInputStream(oldPath)) {
                try (PDDocument doc = PDDocument.load(newIn); PDDocument oldDoc = PDDocument.load(oldIn)) {
                    for (int i = 0, j = oldDoc.getNumberOfPages(); i < j; i++) {
                        String page = Strings.padStart(String.valueOf(i), String.valueOf(j).length(), '0');
                        this.compareImage(key
                                , new PDFRenderer(doc).renderImage(i)
                                , new PDFRenderer(oldDoc).renderImage(i)
                                , it -> new File(this.resultDir, it.getKeysToString() + page + ".png"));
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