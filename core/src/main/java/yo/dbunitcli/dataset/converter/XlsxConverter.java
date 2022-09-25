package yo.dbunitcli.dataset.converter;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.DataSetConsumerParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class XlsxConverter extends XlsConverter {

    public XlsxConverter(DataSetConsumerParam param) {
        super(param);
    }

    @Override
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook(File writeTo) throws DataSetException {
        SXSSFWorkbook result;
        if (writeTo == null) {
            result = new SXSSFWorkbook(1000);
            result.setCompressTempFiles(true);
            Font font = result.getFontAt(0);
            font.setFontName("МＳ ゴシック");
            font.setFontHeightInPoints((short) 8);
        } else {
            try (FileInputStream is = new FileInputStream(writeTo)) {
                result = new SXSSFWorkbook(new XSSFWorkbook(is), 1000, true);
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }
        return result;
    }
}
