package yo.dbunitcli.dataset.writer;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.IDataSetWriter;

public class XlsxDataSetWriter extends XlsDataSetWriter implements IDataSetWriter {

    public XlsxDataSetWriter(DataSetWriterParam param) {
        super(param);
    }

    @Override
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook() {
        Workbook result = new SXSSFWorkbook();
        Font font = result.getFontAt(0);
        font.setFontName("МＳ　ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }
}
