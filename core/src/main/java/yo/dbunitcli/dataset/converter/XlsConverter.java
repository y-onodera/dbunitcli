package yo.dbunitcli.dataset.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class XlsConverter implements IDataSetConverter {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ZEROS = "0000000000000000000000000000000000000000000000000000";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected final File resultDir;

    protected String filename;

    protected final TableExportType tableExport;

    protected final boolean exportEmptyTable;

    private final Map<Workbook, Map<Short, CellStyle>> cellStyleMap = new HashMap<>();

    private Workbook workbook;

    private ITableMetaData metaData;

    private Sheet sheet;

    private int sheetIndex = 0;

    private int rowIndex = 1;

    public XlsConverter(final DataSetConsumerParam param) {
        this(param.resultDir()
                , param.fileName()
                , TableExportType.valueOf(param.excelTable())
                , param.exportEmptyTable());
    }

    public XlsConverter(final File resultDir, final String filename, final TableExportType tableExport, final boolean exportEmptyTable) {
        this.resultDir = resultDir;
        this.filename = Optional.ofNullable(filename).orElse(this.resultDir.getName());
        this.tableExport = tableExport;
        this.exportEmptyTable = exportEmptyTable;
    }

    @Override
    public boolean isSplittable() {
        return this.tableExport != TableExportType.SHEET;
    }

    @Override
    public IDataSetConverter split() {
        return new XlsConverter(this.resultDir, this.filename, this.tableExport, this.exportEmptyTable);
    }

    @Override
    public void startDataSet() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        LOGGER.info("convert - start sheetName={}", metaData.getTableName());
        this.metaData = metaData;
        if (this.tableExport == TableExportType.BOOK) {
            this.filename = this.metaData.getTableName();
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
        this.sheet = this.workbook.createSheet(this.metaData.getTableName());
        this.workbook.setSheetName(this.sheetIndex++, this.metaData.getTableName());
        final Row headerRow = this.sheet.createRow(0);
        final Column[] columns = this.metaData.getColumns();
        IntStream.range(0, columns.length).forEach(i -> headerRow.createCell(i).setCellValue(columns[i].getColumnName()));
    }

    @Override
    public void reStartTable(final ITableMetaData metaData, final Integer writeRows) {
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
    public void row(final Object[] objects) throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        final Row row = this.sheet.createRow(this.rowIndex++);
        IntStream.range(0, columns.length).forEach(k -> {
            final Object value = objects[k];
            if (value != null) {
                final Cell cell = row.createCell(k);
                if (value instanceof Date) {
                    this.setDateCell(cell, (Date) value);
                } else if (value instanceof BigDecimal) {
                    this.setNumericCell(cell, (BigDecimal) value, this.workbook);
                } else if (value instanceof Long) {
                    this.setDateCell(cell, new Date((Long) value));
                } else {
                    final String stringValue = this.getString(value);
                    if (!Optional.ofNullable(stringValue).orElse("").isEmpty()) {
                        cell.setCellValue(stringValue);
                    }
                }
            }
        });
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
    public File getDir() {
        return this.resultDir;
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    protected void flush() {
        final File writeTo = this.writeTo();
        LOGGER.info("flush - start fileName={}", writeTo);
        try {
            if (!this.resultDir.exists()) {
                Files.createDirectories(this.resultDir.toPath());
            }
            try (final FileOutputStream out = new FileOutputStream(writeTo)) {
                this.workbook.write(out);
                out.flush();
                if (this.workbook instanceof SXSSFWorkbook) {
                    ((SXSSFWorkbook) this.workbook).dispose();
                }
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        LOGGER.info("flush - end   fileName={}", writeTo);
    }

    protected File writeTo() {
        return new File(this.resultDir, this.getFilename());
    }

    protected String getFilename() {
        return this.filename + ".xls";
    }

    protected void setNumericCell(final Cell cell, final BigDecimal value, final Workbook workbook) {
        if (value.toPlainString().length() < 16) {
            cell.setCellValue(value.doubleValue());
            final DataFormat df = workbook.createDataFormat();
            final short format;
            if (value.scale() <= 0) {
                format = df.getFormat("####");
            } else {
                final String zeros = createZeros(value.scale());
                format = df.getFormat("####." + zeros);
            }
            cell.setCellStyle(this.getCellStyle(workbook, format));
        } else {
            cell.setCellValue(value.toPlainString());
        }
    }

    protected void setDateCell(final Cell cell, final Date value) {
        cell.setCellValue(SDF.format(value));
    }

    protected Workbook createWorkbook() {
        return this.createWorkbook(null);
    }

    protected Workbook createWorkbook(final File writeTo) {
        final Workbook result;
        if (writeTo == null) {
            result = new HSSFWorkbook();
            final Font font = result.getFontAt(0);
            font.setFontName("МＳ ゴシック");
            font.setFontHeightInPoints((short) 8);
        } else {
            try (final FileInputStream stream = new FileInputStream(writeTo)) {
                result = WorkbookFactory.create(stream);
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }
        return result;
    }

    protected CellStyle getCellStyle(final Workbook workbook, final short formatCode) {
        return this.findCellStyle(workbook, formatCode, this.findWorkbookCellStyleMap(workbook));
    }

    protected Map<Short, CellStyle> findWorkbookCellStyleMap(final Workbook workbook) {
        return this.cellStyleMap.computeIfAbsent(workbook, k -> new HashMap<>());
    }

    protected CellStyle findCellStyle(final Workbook workbook, final Short formatCode, final Map<Short, CellStyle> map) {
        CellStyle cellStyle = map.get(formatCode);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(formatCode);
            map.put(formatCode, cellStyle);
        }
        return cellStyle;
    }

    protected static String createZeros(final int count) {
        return ZEROS.substring(0, count);
    }

    public enum TableExportType {
        SHEET, BOOK
    }

    private String getString(final Object value) {
        try {
            return DataType.asString(value);
        } catch (final TypeCastException e) {
            throw new AssertionError(e);
        }
    }
}
