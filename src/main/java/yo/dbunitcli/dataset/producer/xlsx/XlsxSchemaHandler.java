package yo.dbunitcli.dataset.producer.xlsx;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;

public class XlsxSchemaHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
    private final IDataSetConsumer delegate;
    private final XlsxRowsToTableBuilder rowsTableBuilder;
    private final XlsxCellsToTableBuilder randomCellRecordBuilder;
    private int currentRow = -1;
    private int currentCol = -1;
    private final boolean loadData;

    public XlsxSchemaHandler(IDataSetConsumer delegate, String sheetName, XlsxSchema schema, boolean loadData) {
        this.delegate = delegate;
        this.rowsTableBuilder = schema.getRowsTableBuilder(sheetName);
        this.randomCellRecordBuilder = schema.getCellRecordBuilder(sheetName);
        this.loadData = loadData;
    }

    @Override
    public void startRow(int rowNum) {
        this.currentRow = rowNum;
        this.currentCol = -1;
    }

    @Override
    public void endRow(int rowNum) {
        try {
            if (this.rowsTableBuilder.isTableStart(rowNum)) {
                if (this.rowsTableBuilder.isNowProcessing()) {
                    this.delegate.endTable();
                }
                this.delegate.startTable(this.rowsTableBuilder.startNewTable());
            } else if (this.rowsTableBuilder.hasRow(rowNum)) {
                delegate.row(this.rowsTableBuilder.currentRow());
            }
            this.rowsTableBuilder.clearRowValue();
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        // gracefully handle missing CellRef here in a similar way as XSSFCell does
        if (cellReference == null) {
            cellReference = new CellAddress(this.currentRow, this.currentCol).formatAsString();
        }
        CellReference reference = new CellReference(cellReference);
        this.randomCellRecordBuilder.handle(reference, formattedValue);
        this.rowsTableBuilder.handle(reference, this.currentCol, formattedValue);
        this.currentCol = reference.getCol();
    }

    @Override
    public void endSheet() {
        try {
            if (this.rowsTableBuilder.isNowProcessing()) {
                delegate.endTable();
            }
            for (String tableName : this.randomCellRecordBuilder.getTableNames()) {
                delegate.startTable(this.randomCellRecordBuilder.getTableMetaData(tableName));
                if (this.loadData) {
                    for (Object[] row : this.randomCellRecordBuilder.getRows(tableName)) {
                        delegate.row(row);
                    }
                }
                delegate.endTable();
            }
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }
}
