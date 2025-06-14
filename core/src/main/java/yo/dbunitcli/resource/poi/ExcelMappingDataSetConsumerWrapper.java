package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.util.Optional;
import java.util.stream.Stream;

public class ExcelMappingDataSetConsumerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelMappingDataSetConsumerWrapper.class);
    protected final boolean loadData;
    protected final String[] headerNames;
    private final XlsxSchema schema;
    private final int startRow;
    protected boolean addFileInfo;
    protected IDataSetConsumer consumer;
    protected XlsxCellsToTableBuilder randomCellRecordBuilder;
    protected XlsxRowsToTableBuilder rowsTableBuilder;
    private int rowTableRows = 0;

    public ExcelMappingDataSetConsumerWrapper(final IDataSetConsumer consumer
            , final XlsxSchema schema
            , final int startRow
            , final String[] headerNames
            , final boolean loadData
            , final boolean addFileInfo) {
        this.consumer = consumer;
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
                    this.consumer.endTable();
                    ExcelMappingDataSetConsumerWrapper.LOGGER.info("produce - rows={}", this.rowTableRows);
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
                        this.consumer.endTable();
                        ExcelMappingDataSetConsumerWrapper.LOGGER.info("produce - rows={}", this.rowTableRows);
                    }
                    this.consumer.startTable(this.rowsTableBuilder.startNewTable());
                    this.rowTableRows = 0;
                    if (this.rowsTableBuilder.hasRow(rowNumber)) {
                        this.addRowToTable(this.rowsTableBuilder.currentRow());
                        this.rowTableRows++;
                    }
                } else if (this.rowsTableBuilder.hasRow(rowNumber)) {
                    if (this.rowsTableBuilder.hasRow(rowNumber)) {
                        this.addRowToTable(this.rowsTableBuilder.currentRow());
                        this.rowTableRows++;
                    }
                }
                this.rowsTableBuilder.clearRowValue();
            }
        } catch (final DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void createRandomCellTable() throws DataSetException {
        for (final String tableName : this.randomCellRecordBuilder.getTableNames()) {
            this.consumer.startTable(this.randomCellRecordBuilder.getTableMetaData(tableName));
            ExcelMappingDataSetConsumerWrapper.LOGGER.info("produce - start tableName={}", tableName);
            if (this.loadData) {
                int i = 0;
                for (final String[] row : this.randomCellRecordBuilder.getRows(tableName)) {
                    this.addRowToTable(row);
                    i++;
                }
                ExcelMappingDataSetConsumerWrapper.LOGGER.info("produce - rows={},tableName={}", i, tableName);
            }
            this.consumer.endTable();
            ExcelMappingDataSetConsumerWrapper.LOGGER.info("produce - end   tableName={}", tableName);
        }
    }

    protected void addRowToTable(final String[] row) throws DataSetException {
        if (Stream.of(row).anyMatch(it -> !Optional.ofNullable(it).orElse("").isEmpty())) {
            this.consumer.row(Stream.of(row).map(it -> it == null ? "" : it).toArray());
        }
    }
}
