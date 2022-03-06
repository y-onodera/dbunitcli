package yo.dbunitcli.resource.poi;

import com.google.common.collect.Lists;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.Arrays;
import java.util.List;

public class FirstRowAsColumnTableBuilder implements XlsxRowsToTableBuilder {

    private final String tableName;
    private List<Object> rowValues = Lists.newArrayList();
    private ITableMetaData nowProcessing = null;

    public FirstRowAsColumnTableBuilder(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public boolean isTableStart(int rowNum) {
        return rowNum == 0;
    }

    @Override
    public boolean hasRow(int rowNum) {
        return rowNum > 0;
    }

    @Override
    public ITableMetaData startNewTable() {
        final Column[] columns = new Column[rowValues.size()];
        for (int i = 0, j = rowValues.size(); i < j; i++) {
            columns[i] = new Column(rowValues.get(i).toString(), DataType.UNKNOWN);
        }
        this.nowProcessing = new DefaultTableMetaData(this.tableName, columns);
        return this.nowProcessing;
    }

    @Override
    public void clearRowValue() {
        this.rowValues.clear();
    }

    @Override
    public void handle(CellReference reference, int currentCol, String formattedValue) {
        int thisCol = reference.getCol();
        int missedCols = thisCol - currentCol - 1;
        for (int i = 0; i < missedCols; i++) {
            this.rowValues.add("");
        }
        this.rowValues.add(formattedValue);
    }

    @Override
    public String[] currentRow() throws DataSetException {
        if (this.nowProcessing.getColumns().length < rowValues.size()) {
            throw new AssertionError(rowValues + " large items than header:" + Arrays.toString(this.nowProcessing.getColumns()));
        } else if (rowValues.size() < this.nowProcessing.getColumns().length) {
            for (int i = rowValues.size(), j = this.nowProcessing.getColumns().length; i < j; i++) {
                rowValues.add("");
            }
        }
        return this.rowValues.toArray(new String[0]);
    }

    @Override
    public boolean isNowProcessing() {
        return this.nowProcessing != null;
    }
}
