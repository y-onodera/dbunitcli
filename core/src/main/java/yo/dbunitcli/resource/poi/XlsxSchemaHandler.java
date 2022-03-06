package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.dbunit.dataset.stream.IDataSetConsumer;

public class XlsxSchemaHandler extends ExcelMappingDataSetConsumerWrapper implements XSSFSheetXMLHandler.SheetContentsHandler {
    private int currentRow = -1;
    private int currentCol = -1;

    public XlsxSchemaHandler(IDataSetConsumer delegate, String sheetName, XlsxSchema schema, boolean loadData) {
        super(delegate, schema, loadData);
        this.handleSheetStart(sheetName);
    }

    @Override
    public void startRow(int rowNum) {
        this.currentRow = rowNum;
        this.currentCol = -1;
    }

    @Override
    public void endRow(int rowNum) {
        this.addNewRowToRowsTable(rowNum);
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        // gracefully handle missing CellRef here in a similar way as XSSFCell does
        if (cellReference == null) {
            cellReference = new CellAddress(this.currentRow, this.currentCol).formatAsString();
        }
        CellReference reference = new CellReference(cellReference);
        this.handleCellValue(this.currentCol, formattedValue, reference);
        this.currentCol = reference.getCol();
    }

    @Override
    public void endSheet() {
        this.handleSheetEnd();
    }

}
