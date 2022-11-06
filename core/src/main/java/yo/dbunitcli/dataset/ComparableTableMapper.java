package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;

import java.util.*;
import java.util.stream.IntStream;

public class ComparableTableMapper {

    private final AddSettingTableMetaData addSettingTableMetaData;
    private final Column[] orderColumns;
    private final List<AddSettingTableMetaData> settingChain = Lists.newArrayList();
    private final List<Integer> filteredRowIndexes;
    private final List<Object[]> values;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;
    private IDataSetConverter converter;
    private Map<String, Integer> alreadyWrite;

    public ComparableTableMapper(final AddSettingTableMetaData metaData, final Column[] orderColumns, final List<AddSettingTableMetaData> settings) {
        this.values = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
        this.addSettingTableMetaData = metaData;
        this.orderColumns = orderColumns;
        this.settingChain.addAll(settings);
    }

    public void setConsumer(final IDataSetConverter converter, final Map<String, Integer> alreadyWrite) throws DataSetException {
        this.converter = converter;
        this.alreadyWrite = alreadyWrite;
        if (this.isEnableRowProcessing() && this.converter.isExportEmptyTable()) {
            if (this.alreadyWrite.containsKey(this.addSettingTableMetaData.getTableName())) {
                this.converter.reStartTable(this.addSettingTableMetaData, this.alreadyWrite.get(this.addSettingTableMetaData.getTableName()));
            } else {
                this.converter.startTable(this.addSettingTableMetaData);
            }
            this.startTable = true;
        }
    }

    public ComparableTable endTable() throws DataSetException {
        this.alreadyWrite.compute(this.addSettingTableMetaData.getTableName()
                , (key, old) -> Optional.ofNullable(old).orElse(0) + this.addRowCount);
        if (this.isEnableRowProcessing() && this.startTable) {
            this.converter.endTable();
            return null;
        }
        return new ComparableTable(this.addSettingTableMetaData, this.orderColumns, this.values, this.filteredRowIndexes);
    }

    public String getTargetTableName() {
        return this.addSettingTableMetaData.getTableName();
    }

    public void addRow(final Object[] values) {
        Object[] applySettings = values;
        for (final AddSettingTableMetaData metaData : this.settingChain) {
            applySettings = metaData.applySetting(applySettings);
            if (applySettings == null) {
                break;
            }
        }
        if (applySettings != null) {
            this.add(applySettings);
        }
    }

    public void add(final ComparableTable other) {
        IntStream.range(0, other.getRowCount()).forEach(rowNum ->
                this.addValue(Arrays.stream(this.addSettingTableMetaData.getColumns())
                        .map(column -> other.getValue(rowNum, column.getColumnName()))
                        .toArray(Object[]::new)));
    }

    public void add(final Object[] row) {
        this.addValue(this.addSettingTableMetaData.applySetting(row));
    }

    protected void addValue(final Object[] applySetting) {
        try {
            if (applySetting != null) {
                this.addRowCount++;
                if (this.isEnableRowProcessing()) {
                    if (!this.converter.isExportEmptyTable() && !this.startTable) {
                        this.converter.startTable(this.addSettingTableMetaData);
                        this.startTable = true;
                    }
                    this.converter.row(applySetting);
                } else {
                    this.values.add(applySetting);
                    if (this.addSettingTableMetaData.hasRowFilter()) {
                        this.filteredRowIndexes.add(this.addCount);
                    }
                }
            }
            this.addCount++;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private boolean isEnableRowProcessing() {
        return this.converter != null && this.orderColumns.length == 0;
    }

}
