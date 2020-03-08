package yo.dbunitcli.mapper.xlsx;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

public interface XlsxRowsToTableBuilder {

    boolean isTableStart(int rowNum);

    boolean hasRow(int rowNum) throws DataSetException;

    ITableMetaData startNewTable();

    void clearRowValue();

    void handle(CellReference reference, int currentCol, String formattedValue);

    Object[] currentRow() throws DataSetException;

    boolean isNowProcessing();

}
