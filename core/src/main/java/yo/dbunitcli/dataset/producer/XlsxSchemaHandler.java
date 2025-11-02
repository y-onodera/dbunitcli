package yo.dbunitcli.dataset.producer;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.resource.poi.XlsxSchema;

public class XlsxSchemaHandler extends ExcelMappingDataSetProducer implements XSSFSheetXMLHandler.SheetContentsHandler {
    private int currentRow = -1;
    private int currentCol = -1;

    public XlsxSchemaHandler(final ComparableTableMappingContext context
            , final XlsxSchema schema
            , final int startRow
            , final String[] headerNames
            , final boolean loadData
            , final Source source) {
        super(schema, startRow, headerNames, loadData, source.addFileInfo());
        this.context = context;
        this.handleSheetStart(headerNames, source);
    }

    @Override
    public void startRow(final int rowNum) {
        this.currentRow = rowNum;
        this.currentCol = -1;
    }

    @Override
    public void endRow(final int rowNum) {
        this.addNewRowToRowsTable(rowNum);
    }

    @Override
    public void cell(String cellReference, final String formattedValue, final XSSFComment comment) {
        // gracefully handle missing CellRef here in a similar way as XSSFCell does
        if (cellReference == null) {
            cellReference = new CellAddress(this.currentRow, this.currentCol).formatAsString();
        }
        final CellReference reference = new CellReference(cellReference);
        this.handleCellValue(this.currentCol, formattedValue, reference);
        this.currentCol = reference.getCol();
    }

    @Override
    public void endSheet() {
        this.handleSheetEnd();
    }

}
