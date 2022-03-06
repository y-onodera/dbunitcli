package yo.dbunitcli.resource.poi;

import com.google.common.base.Strings;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.util.stream.Stream;

public class ExcelMappingDataSetConsumerWrapper {

    protected final boolean loadData;
    private final XlsxSchema schema;
    protected IDataSetConsumer consumer;
    protected XlsxCellsToTableBuilder randomCellRecordBuilder;
    protected XlsxRowsToTableBuilder rowsTableBuilder;

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
                }
                this.consumer.startTable(this.rowsTableBuilder.startNewTable());
            } else if (this.rowsTableBuilder.hasRow(rowNumber)) {
                if (this.rowsTableBuilder.hasRow(rowNumber)) {
                    this.addRowToTable(this.rowsTableBuilder.currentRow());
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
            if (this.loadData) {
                for (String[] row : this.randomCellRecordBuilder.getRows(tableName)) {
                    this.addRowToTable(row);
                }
            }
            this.consumer.endTable();
        }
    }

    protected void addRowToTable(String[] row) throws DataSetException {
        if (Stream.of(row).anyMatch(it -> !Strings.isNullOrEmpty(it))) {
            this.consumer.row(Stream.of(row).map(it -> it == null ? "" : it).toArray());
        }
    }
}
