package yo.dbunitcli.dataset;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;

public class XlsxDataSetWriter extends XlsDataSetWriter implements IDataSetWriter {

    public XlsxDataSetWriter(File resultDir) {
        super(resultDir);
    }

    @Override
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook() {
        Workbook result = new XSSFWorkbook();
        Font font = result.getFontAt(0);
        font.setFontName("МＳ　ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }
}
