package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

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
    private final Source source;
    private TableMetaDataWithSource nowProcessing = null;

    public StartRowAsColumnTableBuilder(final int startRow, final String[] headerNames, final Source source) {
        this.source = source;
        this.tableName = this.source.sheetName();
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
    public TableMetaDataWithSource startNewTable() {
        this.nowProcessing = this.source.wrap(new DefaultTableMetaData(this.tableName, this.getColumns()));
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
        if (this.nowProcessing.getColumnLength() < this.rowValues.size()) {
            throw new AssertionError(this.rowValues + " large items than header:" + Arrays.toString(this.nowProcessing.getColumns()));
        }
        return this.nowProcessing.withDefaultValuesToArray(this.rowValues);
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

}