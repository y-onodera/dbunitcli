package yo.dbunitcli.dataset.consumer;

import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.*;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XlsConsumer extends org.dbunit.dataset.excel.XlsDataSetWriter implements IDataSetConsumer {

    private static final Logger logger = LoggerFactory.getLogger(XlsConsumer.class);

    private final File resultDir;

    private String filename;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final TableExportType tableExport;

    private final boolean exportEmptyTable;

    protected Workbook workbook;

    private ITableMetaData metaData;

    private Sheet sheet;

    private int sheetIndex = 0;

    private int rowIndex = 1;

    public XlsConsumer(DataSetConsumerParam param) {
        this.resultDir = param.getResultDir();
        this.tableExport = TableExportType.valueOf(param.getExcelTable());
        this.exportEmptyTable = param.isExportEmptyTable();
        this.filename = param.getFileName();
    }

    @Override
    public void startDataSet() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        logger.info("writeToSheet(sheetName={}) - start", metaData.getTableName());
        this.metaData = metaData;
        if (this.tableExport == TableExportType.BOOK) {
            this.filename = this.metaData.getTableName();
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
        this.sheet = this.workbook.createSheet(this.metaData.getTableName());
        this.workbook.setSheetName(this.sheetIndex, this.metaData.getTableName());
        Row headerRow = this.sheet.createRow(0);
        Column[] columns = this.metaData.getColumns();
        for (int i = 0, j = columns.length; i < j; i++) {
            Column column = columns[i];
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column.getColumnName());
        }
    }

    @Override
    public void row(Object[] objects) throws DataSetException {
        Column[] columns = this.metaData.getColumns();
        Row row = this.sheet.createRow(this.rowIndex++);
        for (int k = 0; k < columns.length; ++k) {
            Object value = objects[k];
            if (value != null) {
                Cell cell = row.createCell(k);
                if (value instanceof Date) {
                    this.setDateCell(cell, (Date) value, workbook);
                } else if (value instanceof BigDecimal) {
                    this.setNumericCell(cell, (BigDecimal) value, workbook);
                } else if (value instanceof Long) {
                    this.setDateCell(cell, new Date((Long) value), workbook);
                } else {
                    String stringValue = DataType.asString(value);
                    if (!Strings.isNullOrEmpty(stringValue)) {
                        cell.setCellValue(stringValue);
                    }
                }
            }
        }
    }

    @Override
    public void endTable() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.sheetIndex++;
        } else {
            this.flush();
        }
        this.rowIndex = 1;
    }

    @Override
    public void endDataSet() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.flush();
        }
    }

    protected void flush() throws DataSetException {
        File writeTo = new File(this.resultDir, getFilename());
        logger.info("writeToFile(fileName={}) - start", writeTo);
        if (!this.resultDir.exists()) {
            this.resultDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(writeTo)) {
            this.workbook.write(out);
            out.flush();
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void cleanupDirectory() {
        if (this.resultDir.exists()) {
            this.resultDir.delete();
        }
    }

    @Override
    public void open(String aFileName) {
        this.filename = aFileName;
    }

    @Override
    public File getDir() {
        return this.resultDir;
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
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
        font.setFontName("МＳ ゴシック");
        font.setFontHeightInPoints((short) 8);
        return result;
    }

    public enum TableExportType {
        SHEET, BOOK
    }

}
