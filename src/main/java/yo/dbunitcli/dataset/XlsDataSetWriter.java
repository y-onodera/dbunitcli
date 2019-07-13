package yo.dbunitcli.dataset;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XlsDataSetWriter extends org.dbunit.dataset.excel.XlsDataSetWriter implements IDataSetWriter {

    private final File resultDir;

    private String filename;

    private DefaultDataSet dataSet;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public XlsDataSetWriter(File resultDir) {
        this.resultDir = resultDir;
    }

    @Override
    public void open(String aFileName) {
        this.filename = aFileName;
        this.dataSet = new DefaultDataSet();
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        this.dataSet.addTable(new SortedTable(aTable));
    }

    @Override
    public void close() throws DataSetException {
        try {
            if (!this.resultDir.exists()) {
                this.resultDir.mkdirs();
            }
            this.write(this.dataSet, new FileOutputStream(new File(this.resultDir, getFilename())));
        } catch (IOException | DataSetException e) {
            throw new DataSetException(e);
        }
    }

    protected String getFilename() {
        return this.filename + ".xls";
    }

    @Override
    protected void setNumericCell(Cell cell, BigDecimal value, Workbook workbook) {
        if (value.toPlainString().length() < 16) {
            super.setNumericCell(cell, value, workbook);
        } else {
            cell.setCellType(CellType.STRING);
            cell.setCellValue(value.toPlainString());
        }
    }

    @Override
    protected void setDateCell(Cell cell, Date value, Workbook workbook) {
        cell.setCellType(CellType.STRING);
        cell.setCellValue(sdf.format(value));
    }

    @Override
    protected Workbook createWorkbook() {
        Workbook result = super.createWorkbook();
        Font font = result.getFontAt(0);
        font.setFontName("МＳ　ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }
}
