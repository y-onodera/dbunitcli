package yo.dbunitcli.dataset.consumer;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.DataSetConsumerParam;

public class XlsxConsumer extends XlsConsumer {

    public XlsxConsumer(DataSetConsumerParam param) {
        super(param);
    }

    @Override
    public void endDataSet() throws DataSetException {
        super.endDataSet();
    }

    @Override
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook() {
        Workbook result = new SXSSFWorkbook(1000);
        Font font = result.getFontAt(0);
        font.setFontName("МＳ ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }
}
