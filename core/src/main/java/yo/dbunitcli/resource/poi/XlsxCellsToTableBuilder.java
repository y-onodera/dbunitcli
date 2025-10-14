package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class XlsxCellsToTableBuilder {

    public static XlsxCellsToTableBuilder NO_TARGET = new XlsxCellsToTableBuilder(new ArrayList<>(), Source.NONE) {
        @Override
        public void handle(final CellReference reference, final String formattedValue) {
            // no handle
        }
    };
    private final String[] tableNames;

    private final Map<String, TableMetaDataWithSource> tableMetaDataMap = new HashMap<>();

    private final Map<String, List<XlsxCellsTableDefine>> columnDefine = new HashMap<>();

    private final Map<String, List<String[]>> row = new HashMap<>();

    public XlsxCellsToTableBuilder(final List<XlsxCellsTableDefine> tableDefines, final Source source) {
        this.tableNames = new String[tableDefines.size()];
        IntStream.range(0, this.tableNames.length).forEach(i -> {
            final XlsxCellsTableDefine def = tableDefines.get(i);
            final Source sourceWithTableDefine = source.addFileInfo(def.addFileInfo() || source.addFileInfo());
            this.tableNames[i] = def.tableName();
            final TableMetaDataWithSource metaData = sourceWithTableDefine.wrap(def.tableMetaData());
            this.tableMetaDataMap.put(def.tableName(), metaData);
            def.getTargetAddresses().forEach(cellAddress -> {
                if (!this.columnDefine.containsKey(cellAddress)) {
                    this.columnDefine.put(cellAddress, new ArrayList<>());
                }
                this.columnDefine.get(cellAddress).add(def);
            });
            this.row.put(def.tableName(), new ArrayList<>());
            final List<String[]> targetRows = this.row.get(def.tableName());
            IntStream.range(0, def.rowCount())
                    .forEach(rowIndex -> targetRows.add(rowIndex, metaData.defaultColumnValues()));
        });
    }

    public void handle(final CellReference reference, final String formattedValue) {
        final String ref = reference.formatAsString()
                .replaceAll(".+!", "")
                .replaceAll("\\$", "");
        if (this.columnDefine.containsKey(ref)) {
            this.columnDefine.get(ref).forEach(it -> {
                final ITableMetaData metaData = this.tableMetaDataMap.get(it.tableName());
                try {
                    this.row.get(it.tableName())
                            .get(it.getRowIndex(ref))[metaData.getColumnIndex(it.getColumnName(ref))] = formattedValue;
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
            });
        }
    }

    public String[] getTableNames() {
        return this.tableNames;
    }

    public TableMetaDataWithSource getTableMetaData(final String tableName) {
        return this.tableMetaDataMap.get(tableName);
    }

    public List<String[]> getRows(final String tableName) {
        return this.row.get(tableName);
    }
}
