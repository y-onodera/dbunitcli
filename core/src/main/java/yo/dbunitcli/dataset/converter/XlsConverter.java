package yo.dbunitcli.dataset.converter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.DataSetConverterParam;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsConverter.class);
    private static final String ZEROS = "0000000000000000000000000000000000000000000000000000";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected final File resultDir;
    protected final TableExportType tableExport;
    protected final boolean exportEmptyTable;
    protected final boolean exportHeader;
    private final Map<Workbook, Map<Short, CellStyle>> cellStyleMap = new HashMap<>();
    protected String filename;
    private Workbook workbook;

    private ITableMetaData metaData;

    private Sheet sheet;

    private int sheetIndex = 0;

    private int rowIndex = 1;

    protected static String createZeros(final int count) {
        return XlsConverter.ZEROS.substring(0, count);
    }

    public XlsConverter(final DataSetConverterParam param) {
        this(param.resultDir()
                , param.fileName()
                , TableExportType.valueOf(param.excelTable())
                , param.exportEmptyTable()
                , param.exportHeader());
    }

    public XlsConverter(final File resultDir
            , final String filename
            , final TableExportType tableExport
            , final boolean exportEmptyTable
            , final boolean exportHeader) {
        this.resultDir = resultDir;
        this.filename = Optional.ofNullable(filename).orElse(this.resultDir.getName());
        this.tableExport = tableExport;
        this.exportEmptyTable = exportEmptyTable;
        this.exportHeader = exportHeader;
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData metaData, final Integer writeRows) {
        XlsConverter.LOGGER.info("convert - reStart sheetName={},rows={}", metaData.getTableName(), writeRows);
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
    public File getDir() {
        return this.resultDir;
    }

    @Override
    public boolean isSplittable() {
        return this.tableExport != TableExportType.SHEET;
    }

    @Override
    public IDataSetConverter split() {
        return new XlsConverter(this.resultDir, this.filename, this.tableExport, this.exportEmptyTable, this.exportHeader);
    }

    @Override
    public void startDataSet() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
    }

    @Override
    public void endDataSet() throws DataSetException {
        if (this.tableExport == TableExportType.SHEET) {
            this.flush();
        }
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        XlsConverter.LOGGER.info("convert - start sheetName={}", metaData.getTableName());
        this.metaData = metaData;
        if (this.tableExport == TableExportType.BOOK) {
            this.filename = this.metaData.getTableName();
            this.workbook = this.createWorkbook();
            this.sheetIndex = 0;
        }
        this.sheet = this.workbook.createSheet(this.metaData.getTableName());
        this.sheet.setDisplayZeros(true);
        this.workbook.setSheetName(this.sheetIndex++, this.metaData.getTableName());
        if (this.exportHeader) {
            this.rowIndex = 1;
            this.writeColumnNames();
        } else {
            this.rowIndex = 0;
        }
    }

    @Override
    public void endTable() throws DataSetException {
        XlsConverter.LOGGER.info("convert - rows={} ", this.rowIndex - (this.exportHeader ? 1 : 0));
        XlsConverter.LOGGER.info("convert - end   sheetName={}", this.metaData.getTableName());
        if (this.tableExport == TableExportType.BOOK) {
            this.flush();
        }
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        final Row row = this.sheet.createRow(this.rowIndex++);
        IntStream.range(0, columns.length).forEach(k -> {
            final Object value = objects[k];
            if (value != null) {
                final Cell cell = row.createCell(k);
                switch (value) {
                    case final Date date -> this.setDateCell(cell, date);
                    case final BigDecimal numeric -> this.setNumericCell(cell, numeric, this.workbook);
                    case final Long numeric -> this.setNumericCell(cell, new BigDecimal(numeric), this.workbook);
                    default -> {
                        final String stringValue = this.getString(value);
                        if (!Optional.ofNullable(stringValue).orElse("").isEmpty()) {
                            cell.setCellValue(stringValue);
                        }
                    }
                }
            }
        });
    }

    protected void writeColumnNames() throws DataSetException {
        final Row headerRow = this.sheet.createRow(0);
        final Column[] columns = this.metaData.getColumns();
        IntStream.range(0, columns.length).forEach(i -> headerRow.createCell(i).setCellValue(columns[i].getColumnName()));
    }

    protected void flush() {
        final File writeTo = this.writeTo();
        XlsConverter.LOGGER.info("flush - start fileName={}", writeTo);
        try {
            if (!this.resultDir.exists()) {
                Files.createDirectories(this.resultDir.toPath());
            }
            try (final FileOutputStream out = new FileOutputStream(writeTo)) {
                this.workbook.write(out);
                out.flush();
                if (this.workbook instanceof SXSSFWorkbook) {
                    this.workbook.close();
                }
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        XlsConverter.LOGGER.info("flush - end   fileName={}", writeTo);
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
                format = df.getFormat("0");
            } else {
                final String zeros = XlsConverter.createZeros(value.scale());
                format = df.getFormat("0." + zeros);
            }
            cell.setCellStyle(this.getCellStyle(workbook, format));
        } else {
            cell.setCellValue(value.toPlainString());
        }
    }

    protected void setDateCell(final Cell cell, final Date value) {
        cell.setCellValue(XlsConverter.SDF.format(value));
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

    private String getString(final Object value) {
        try {
            return DataType.asString(value);
        } catch (final TypeCastException e) {
            throw new AssertionError(e);
        }
    }

    public enum TableExportType {
        SHEET, BOOK
    }
}
