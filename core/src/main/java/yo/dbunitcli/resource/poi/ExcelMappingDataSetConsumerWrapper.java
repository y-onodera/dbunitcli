package yo.dbunitcli.resource.poi;

import com.google.common.base.Strings;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.producer.ComparableXlsDataSetProducer;

import java.util.stream.Stream;

public class ExcelMappingDataSetConsumerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelMappingDataSetConsumerWrapper.class);

    protected final boolean loadData;
    private final XlsxSchema schema;
    protected IDataSetConsumer consumer;
    protected XlsxCellsToTableBuilder randomCellRecordBuilder;
    protected XlsxRowsToTableBuilder rowsTableBuilder;
    private int rowTableRows = 0;

    public ExcelMappingDataSetConsumerWrapper(IDataSetConsumer consumer, XlsxSchema schema, boolean loadData) {
        this.consumer = consumer;
        this.schema = schema;
        this.loadData = loadData;
    }

    protected void handleSheetStart(String tableName) {
        this.rowsTableBuilder = this.schema.getRowsTableBuilder(tableName);
        this.randomCellRecordBuilder = this.schema.getCellRecordBuilder(tableName);
    }

    protected void handleSheetEnd() {
        try {
            if (this.rowsTableBuilder != null) {
                if (this.rowsTableBuilder.isNowProcessing()) {
                    this.consumer.endTable();
                    LOGGER.info("produce - rows={}", this.rowTableRows);
                }
            }
            if (this.randomCellRecordBuilder != null) {
                this.createRandomCellTable();
            }
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleCellValue(int thisColumn, String thisStr, CellReference reference) {
        this.randomCellRecordBuilder.handle(reference, thisStr);
        this.rowsTableBuilder.handle(reference, thisColumn, thisStr);
    }

    protected void addNewRowToRowsTable(int rowNumber) {
        try {
            if (this.rowsTableBuilder.isTableStart(rowNumber)) {
                if (this.rowsTableBuilder.isNowProcessing()) {
                    this.consumer.endTable();
                    LOGGER.info("produce - rows={}", this.rowTableRows);
                }
                this.consumer.startTable(this.rowsTableBuilder.startNewTable());
                this.rowTableRows = 0;
            } else if (this.rowsTableBuilder.hasRow(rowNumber)) {
                if (this.rowsTableBuilder.hasRow(rowNumber)) {
                    this.addRowToTable(this.rowsTableBuilder.currentRow());
                    this.rowTableRows++;
                }
            }
            this.rowsTableBuilder.clearRowValue();
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    protected void createRandomCellTable() throws DataSetException {
        for (String tableName : this.randomCellRecordBuilder.getTableNames()) {
            this.consumer.startTable(this.randomCellRecordBuilder.getTableMetaData(tableName));
            LOGGER.info("produce - start tableName={}", tableName);
            if (this.loadData) {
                int i = 0;
                for (String[] row : this.randomCellRecordBuilder.getRows(tableName)) {
                    this.addRowToTable(row);
                    i++;
                }
                LOGGER.info("produce - rows={},tableName={}", i, tableName);
            }
            this.consumer.endTable();
            LOGGER.info("produce - end   tableName={}", tableName);
        }
    }

    protected void addRowToTable(String[] row) throws DataSetException {
        if (Stream.of(row).anyMatch(it -> !Strings.isNullOrEmpty(it))) {
            this.consumer.row(Stream.of(row).map(it -> it == null ? "" : it).toArray());
        }
    }
}
