package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import yo.dbunitcli.dataset.TableMetaDataWithSource;

public interface XlsxRowsToTableBuilder {

    boolean isTableStart(int rowNum);

    boolean hasRow(int rowNum);

    TableMetaDataWithSource startNewTable();

    void clearRowValue();

    void handle(CellReference reference, int currentCol, String formattedValue);

    String[] currentRow();

    boolean isNowProcessing();

}
