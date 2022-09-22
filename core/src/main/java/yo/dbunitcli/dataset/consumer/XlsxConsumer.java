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
    protected String getFilename() {
        return super.getFilename() + "x";
    }

    @Override
    protected Workbook createWorkbook() {
        SXSSFWorkbook result = new SXSSFWorkbook(1000);
        result.setCompressTempFiles(true);
        Font font = result.getFontAt(0);
        font.setFontName("МＳ ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }
}
