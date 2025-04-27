package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StartRowAsColumnTableBuilder implements XlsxRowsToTableBuilder {

    private final String tableName;
    private final List<String> rowValues = new ArrayList<>();
    private final String[] headerNames;
    private final int startRow;
    private ITableMetaData nowProcessing = null;

    public StartRowAsColumnTableBuilder(final String tableName, final int startRow, final String[] headerNames) {
        this.tableName = tableName;
        this.headerNames = headerNames;
        this.startRow = startRow;
    }

    @Override
    public boolean isTableStart(final int rowNum) {
        return rowNum + 1 == this.startRow;
    }

    @Override
    public boolean hasRow(final int rowNum) {
        return rowNum + (this.headerNames != null ? 1 : 0) >= this.startRow;
    }

    @Override
    public ITableMetaData startNewTable() {
        this.nowProcessing = new DefaultTableMetaData(this.tableName, this.getColumns());
        return this.nowProcessing;
    }

    @Override
    public void clearRowValue() {
        this.rowValues.clear();
    }

    @Override
    public void handle(final CellReference reference, final int currentCol, final String formattedValue) {
        if (reference.getRow() + 1 < this.startRow) {
            return;
        }
        final int thisCol = reference.getCol();
        final int missedCols = thisCol - currentCol - 1;
        IntStream.range(0, missedCols).forEach(i -> this.rowValues.add(""));
        this.rowValues.add(formattedValue);
    }

    @Override
    public String[] currentRow() {
        if (this.getNowProcessingColumns().length < this.rowValues.size()) {
            throw new AssertionError(this.rowValues + " large items than header:" + Arrays.toString(this.getNowProcessingColumns()));
        } else if (this.rowValues.size() < this.getNowProcessingColumns().length) {
            IntStream.range(this.rowValues.size(), this.getNowProcessingColumns().length)
                    .forEach(i -> this.rowValues.add(""));
        }
        return this.rowValues.toArray(new String[0]);
    }

    @Override
    public boolean isNowProcessing() {
        return this.nowProcessing != null;
    }

    private Column[] getColumns() {
        final Stream<String> columnNames = this.headerNames == null ? this.rowValues.stream() : Arrays.stream(this.headerNames);
        return columnNames
                .map(rowValue -> new Column(rowValue, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    private Column[] getNowProcessingColumns() {
        try {
            return this.nowProcessing.getColumns();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}