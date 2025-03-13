package yo.dbunitcli.dataset.converter;

import org.apache.poi.ss.usermodel.FontScheme;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XlsxConverter extends XlsConverter {

    public XlsxConverter(final DataSetConverterParam param) {
        super(param);
    }

    public XlsxConverter(final File resultDir
            , final String filename
            , final TableExportType tableExport
            , final boolean exportEmptyTable
            , final boolean exportHeader) {
        super(resultDir, filename, tableExport, exportEmptyTable, exportHeader);
    }

    @Override
    public IDataSetConverter split() {
        return new XlsxConverter(this.resultDir, this.filename, this.tableExport, this.exportEmptyTable, this.exportHeader);
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
            final XSSFFont font = (XSSFFont) result.getFontAt(0);
            font.setFontName("МＳ ゴシック");
            font.setFontHeightInPoints((short) 8);
            font.setFamily(3);
            font.setScheme(FontScheme.NONE);
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
