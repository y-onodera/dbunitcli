package yo.dbunitcli.dataset.compare;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.dbunit.dataset.DataSetException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfCompare extends ImageCompare {

    public static Builder builder() {
        return new Builder();
    }

    public PdfCompare(ImageCompareBuilder builder) {
        super(builder);
    }

    @Override
    protected boolean compareFile(File oldPath, File newPath, File destination) throws DataSetException {
        try (InputStream newIn = new FileInputStream(newPath);
             InputStream oldIn = new FileInputStream(oldPath)
        ) {
            try (PDDocument doc = PDDocument.load(newIn);
                 PDDocument oldDoc = PDDocument.load(oldIn)
            ) {
                boolean result = true;
                for (int i = 0, j = oldDoc.getNumberOfPages(); i < j; i++) {
                    result = result && this.compareImage(destination, new PDFRenderer(doc).renderImage(i), new PDFRenderer(oldDoc).renderImage(i));
                }
                return result;
            }
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }

    public static class Builder extends ImageCompareBuilder {
        @Override
        public TableDataSetCompare getTableDataSetCompare() {
            return new PdfCompare(this);
        }
    }

}
