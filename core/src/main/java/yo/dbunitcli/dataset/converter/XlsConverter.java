package yo.dbunitcli.dataset.converter;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class XlsConverter implements IDataSetConverter {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ZEROS = "0000000000000000000000000000000000000000000000000000";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<Workbook, Map<Short, CellStyle>> cellStyleMap = new HashMap<>();

    private final File resultDir;

    private String filename;

    private final TableExportType tableExport;

    private final boolean exportEmptyTable;

    private Workbook workbook;

    private ITableMetaData metaData;

    private Sheet sheet;

    private int sheetIndex = 0;

    private int rowIndex = 1;

    public XlsConverter(DataSetConsumerParam param) {
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
        LOGGER.info("convert - start sheetName={}", metaData.getTableName());
        this.metaData = metaData;
        if (this.tableExport == TableExportType.BOOK) {
            this.filename = this.metaData.getTableName();
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
        this.sheet = this.workbook.createSheet(this.metaData.getTableName());
        this.workbook.setSheetName(this.sheetIndex++, this.metaData.getTableName());
        Row headerRow = this.sheet.createRow(0);
        Column[] columns = this.metaData.getColumns();
        for (int i = 0, j = columns.length; i < j; i++) {
            headerRow.createCell(i).setCellValue(columns[i].getColumnName());
        }
    }

    @Override
    public void reStartTable(ITableMetaData metaData, Integer writeRows) throws DataSetException {
        LOGGER.info("convert - reStart sheetName={},rows={}", metaData.getTableName(), writeRows);
        this.metaData = metaData;
        if (this.tableExport == TableExportType.BOOK) {
            this.filename = this.metaData.getTableName();
            this.workbook = this.createWorkbook(this.writeTo());
            this.sheetIndex = 0;
        }
        this.sheet = this.workbook.getSheet(this.metaData.getTableName());
        this.rowIndex = writeRows + 1;
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
                    this.setDateCell(cell, (Date) value);
                } else if (value instanceof BigDecimal) {
                    this.setNumericCell(cell, (BigDecimal) value, this.workbook);
                } else if (value instanceof Long) {
                    this.setDateCell(cell, new Date((Long) value));
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
        LOGGER.info("convert - rows={} ", this.rowIndex - 1);
        LOGGER.info("convert - end   sheetName={}", this.metaData.getTableName());
        if (this.tableExport == TableExportType.BOOK) {
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

    protected void flush() throws DataSetException {
        File writeTo = this.writeTo();
        LOGGER.info("flush - start fileName={}", writeTo);
        if (!this.resultDir.exists()) {
            this.resultDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(writeTo)) {
            this.workbook.write(out);
            out.flush();
            if (this.workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) this.workbook).dispose();
            }
        } catch (IOException e) {
            throw new DataSetException(e);
        }
        LOGGER.info("flush - end   fileName={}", writeTo);
    }

    protected File writeTo() {
        return new File(this.resultDir, this.getFilename());
    }

    protected String getFilename() {
        return this.filename + ".xls";
    }

    protected void setNumericCell(Cell cell, BigDecimal value, Workbook workbook) {
        if (value.toPlainString().length() < 16) {
            cell.setCellValue(value.doubleValue());

            DataFormat df = workbook.createDataFormat();
            int scale = value.scale();
            short format;
            if (scale <= 0) {
                format = df.getFormat("####");
            } else {
                String zeros = createZeros(value.scale());
                format = df.getFormat("####." + zeros);
            }
            CellStyle cellStyleNumber = getCellStyle(workbook, format);
            cell.setCellStyle(cellStyleNumber);
        } else {
            cell.setCellValue(value.toPlainString());
        }
    }

    protected void setDateCell(Cell cell, Date value) {
        cell.setCellValue(SDF.format(value));
    }

    protected Workbook createWorkbook() throws DataSetException {
        return this.createWorkbook(null);
    }

    protected Workbook createWorkbook(File writeTo) throws DataSetException {
        Workbook result;
        if (writeTo == null) {
            result = new HSSFWorkbook();
            Font font = result.getFontAt(0);
            font.setFontName("МＳ ゴシック");
            font.setFontHeightInPoints((short) 8);
        } else {
            try (FileInputStream stream = new FileInputStream(writeTo)) {
                result = WorkbookFactory.create(stream);
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }
        return result;
    }

    protected CellStyle getCellStyle(Workbook workbook, short formatCode) {
        return this.findCellStyle(workbook, formatCode, this.findWorkbookCellStyleMap(workbook));
    }

    protected Map<Short, CellStyle> findWorkbookCellStyleMap(Workbook workbook) {
        return this.cellStyleMap.computeIfAbsent(workbook, k -> new HashMap<>());
    }

    protected CellStyle findCellStyle(Workbook workbook, Short formatCode, Map<Short, CellStyle> map) {
        CellStyle cellStyle = map.get(formatCode);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(formatCode);
            map.put(formatCode, cellStyle);
        }
        return cellStyle;
    }

    protected static String createZeros(int count) {
        return ZEROS.substring(0, count);
    }

    public enum TableExportType {
        SHEET, BOOK
    }

}
