package yo.dbunitcli.dataset.producer;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableTableMapper;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.resource.poi.XlsxCellsToTableBuilder;
import yo.dbunitcli.resource.poi.XlsxRowsToTableBuilder;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.util.Optional;
import java.util.stream.Stream;

public class ExcelMappingDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelMappingDataSetProducer.class);
    protected final boolean loadData;
    protected final String[] headerNames;
    protected final boolean addFileInfo;
    private final XlsxSchema schema;
    private final int startRow;
    protected ComparableTableMappingContext context;
    protected ComparableTableMapper mapper;
    protected XlsxCellsToTableBuilder randomCellRecordBuilder;
    protected XlsxRowsToTableBuilder rowsTableBuilder;
    private int rowTableRows = 0;

    public ExcelMappingDataSetProducer(final XlsxSchema schema
            , final int startRow
            , final String[] headerNames
            , final boolean loadData
            , final boolean addFileInfo) {
        this.schema = schema;
        this.startRow = startRow;
        this.headerNames = headerNames;
        this.loadData = loadData;
        this.addFileInfo = addFileInfo;
    }

    protected void handleSheetStart(final String[] headerNames, final Source source) {
        this.rowsTableBuilder = this.schema.getRowsTableBuilder(this.startRow, headerNames, source);
        this.randomCellRecordBuilder = this.schema.getCellRecordBuilder(headerNames, source);
    }

    protected void handleSheetEnd() {
        try {
            if (this.rowsTableBuilder != null) {
                if (this.rowsTableBuilder.isNowProcessing()) {
                    this.mapper.endTable();
                    ExcelMappingDataSetProducer.LOGGER.info("produce - rows={}", this.rowTableRows);
                }
                this.rowsTableBuilder = null;
            }
            if (this.randomCellRecordBuilder != null) {
                this.createRandomCellTable();
                this.randomCellRecordBuilder = null;
            }
        } catch (final DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleCellValue(final int thisColumn, final String thisStr, final CellReference reference) {
        if (this.randomCellRecordBuilder != null) {
            this.randomCellRecordBuilder.handle(reference, thisStr);
        }
        if (this.rowsTableBuilder != null) {
            this.rowsTableBuilder.handle(reference, thisColumn, thisStr);
        }
    }

    protected void addNewRowToRowsTable(final int rowNumber) {
        try {
            if (this.rowsTableBuilder != null) {
                if (this.rowsTableBuilder.isTableStart(rowNumber)) {
                    if (this.rowsTableBuilder.isNowProcessing()) {
                        this.mapper.endTable();
                        ExcelMappingDataSetProducer.LOGGER.info("produce - rows={}", this.rowTableRows);
                    }
                    this.mapper = this.context.createMapper(this.rowsTableBuilder.startNewTable());
                    this.mapper.startTable();
                    this.rowTableRows = 0;
                }
                this.addRowToRowsTable(rowNumber);
                this.rowsTableBuilder.clearRowValue();
            }
        } catch (final DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * シートの残りを読まなくてもよいか。行単位のテーブルがすべて開始していて、以降の行を使わない場合にtrue。
     * 行を読まない（loadData=false）ならセル指定のテーブルのメタデータは定義から決まり、
     * 行を読む場合も、今のテーブルが行を使わず（hasOutput）、セル指定のテーブルが無ければよい
     */
    protected boolean isMetaDataComplete() {
        if (this.rowsTableBuilder != null && !this.rowsTableBuilder.isAllTablesStarted()) {
            return false;
        }
        return !this.loadData
                || ((this.mapper == null || !this.mapper.hasOutput())
                && (this.randomCellRecordBuilder == null || this.randomCellRecordBuilder.getTableNames().length == 0));
    }

    /**
     * 行を読まない場合（loadData=false・出力しないテーブル）は、行の配列化とmapperへの追加を省く
     */
    private void addRowToRowsTable(final int rowNumber) throws DataSetException {
        if (this.loadData && this.mapper != null && this.mapper.hasOutput() && this.rowsTableBuilder.hasRow(rowNumber)) {
            this.addRowToTable(this.rowsTableBuilder.currentRow());
            this.rowTableRows++;
        }
    }

    protected void createRandomCellTable() throws DataSetException {
        for (final String tableName : this.randomCellRecordBuilder.getTableNames()) {
            this.mapper = this.context.createMapper(this.randomCellRecordBuilder.getTableMetaData(tableName));
            this.mapper.startTable();
            ExcelMappingDataSetProducer.LOGGER.info("produce - start tableName={}", tableName);
            if (this.loadData && this.mapper.hasOutput()) {
                int i = 0;
                for (final String[] row : this.randomCellRecordBuilder.getRows(tableName)) {
                    this.addRowToTable(row);
                    i++;
                }
                ExcelMappingDataSetProducer.LOGGER.info("produce - rows={},tableName={}", i, tableName);
            }
            this.mapper.endTable();
            ExcelMappingDataSetProducer.LOGGER.info("produce - end   tableName={}", tableName);
        }
    }

    protected void addRowToTable(final String[] row) throws DataSetException {
        if (Stream.of(row).anyMatch(it -> !Optional.ofNullable(it).orElse("").isEmpty())) {
            this.mapper.addRow(Stream.of(row).map(it -> it == null ? "" : it).toArray());
        }
    }
}
