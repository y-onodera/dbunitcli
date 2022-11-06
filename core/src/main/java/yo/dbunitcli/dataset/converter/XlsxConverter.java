package yo.dbunitcli.dataset.converter;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import yo.dbunitcli.dataset.DataSetConsumerParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XlsxConverter extends XlsConverter {

    public XlsxConverter(final DataSetConsumerParam param) {
        super(param);
    }

    @Override
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook(final File writeTo) {
        final SXSSFWorkbook result;
        if (writeTo == null) {
            result = new SXSSFWorkbook(1000);
            result.setCompressTempFiles(true);
            final Font font = result.getFontAt(0);
            font.setFontName("МＳ ゴシック");
            font.setFontHeightInPoints((short) 8);
        } else {
            try (final FileInputStream is = new FileInputStream(writeTo)) {
                result = new SXSSFWorkbook(new XSSFWorkbook(is), 1000, true);
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }
        return result;
    }
}
