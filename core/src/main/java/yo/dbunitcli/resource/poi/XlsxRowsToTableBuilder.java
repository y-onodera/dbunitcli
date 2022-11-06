package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.ITableMetaData;

public interface XlsxRowsToTableBuilder {

    boolean isTableStart(int rowNum);

    boolean hasRow(int rowNum);

    ITableMetaData startNewTable();

    void clearRowValue();

    void handle(CellReference reference, int currentCol, String formattedValue);

    String[] currentRow();

    boolean isNowProcessing();

}
