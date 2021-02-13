package yo.dbunitcli.dataset.writer;

import jdk.internal.joptsimple.internal.Strings;
import org.apache.poi.ss.usermodel.*;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XlsDataSetWriter extends org.dbunit.dataset.excel.XlsDataSetWriter implements IDataSetWriter {

    public enum TableExportType {
        SHEET, BOOK
    }

    private static final Logger logger = LoggerFactory.getLogger(XlsDataSetWriter.class);
    private final File resultDir;

    private String filename;

    private DefaultDataSet dataSet;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final TableExportType tableExport;

    private final boolean exportEmptyTable;

    public XlsDataSetWriter(DataSetWriterParam param) {
        this.resultDir = param.getResultDir();
        this.tableExport = TableExportType.valueOf(param.getExcelTable());
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void cleanupDirectory() {
        if (this.resultDir.exists()) {
            this.resultDir.delete();
        }
    }

    @Override
    public void open(String aFileName) {
        if (this.tableExport == TableExportType.SHEET) {
            this.filename = aFileName;
            this.dataSet = new DefaultDataSet();
        }
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        if (!this.exportEmptyTable && aTable.getRowCount() == 0) {
            return;
        }
        if (this.tableExport == TableExportType.SHEET) {
            logger.info("addTable {}", aTable.getTableMetaData().getTableName());
            this.dataSet.addTable(aTable);
        } else {
            this.filename = aTable.getTableMetaData().getTableName();
            this.dataSet = new DefaultDataSet();
            this.dataSet.addTable(aTable);
            this.close();
        }
    }

    @Override
    public void close() throws DataSetException {
        File writeTo = new File(this.resultDir, getFilename());
        logger.info("writeToFile(fileName={}) - start", writeTo);
        try {
            if (!this.resultDir.exists()) {
                this.resultDir.mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(writeTo)) {
                this.write(this.dataSet, out);
            }
        } catch (IOException | DataSetException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void write(IDataSet dataSet, OutputStream out) throws IOException, DataSetException {
        try (Workbook workbook = this.createWorkbook()) {
            int index = 0;

            for (ITableIterator iterator = dataSet.iterator(); iterator.next(); ++index) {
                ITable table = iterator.getTable();
                ITableMetaData metaData = table.getTableMetaData();
                logger.info("writeToSheet(sheetName={}) - start", metaData.getTableName());
                Sheet sheet = workbook.createSheet(metaData.getTableName());
                workbook.setSheetName(index, metaData.getTableName());
                Row headerRow = sheet.createRow(0);
                Column[] columns = metaData.getColumns();

                int j;
                for (j = 0; j < columns.length; ++j) {
                    Column column = columns[j];
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(column.getColumnName());
                }

                for (j = 0; j < table.getRowCount(); ++j) {
                    Row row = sheet.createRow(j + 1);

                    for (int k = 0; k < columns.length; ++k) {
                        Column column = columns[k];
                        Object value = table.getValue(j, column.getColumnName());
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
            }
            workbook.write(out);
            out.flush();
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
