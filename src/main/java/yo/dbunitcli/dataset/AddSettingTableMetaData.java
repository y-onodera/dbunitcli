package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.*;

import java.util.Arrays;
import java.util.Map;

public class AddSettingTableMetaData extends AbstractTableMetaData {
    private final String tableName;
    private final Column[] primaryKeys;
    private final Column[] columns;
    private final ColumnExpression additionalExpression;

    public AddSettingTableMetaData(ITableMetaData delegate, ColumnExpression additionalExpression) throws DataSetException {
        this(delegate.getTableName(), delegate.getPrimaryKeys(), additionalExpression.merge(delegate.getColumns()), additionalExpression);
    }

    public AddSettingTableMetaData(String tableName, Column[] primaryKeys, Column[] columns, ColumnExpression additionalExpression) {
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
        this.additionalExpression = additionalExpression;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public Column[] getColumns() {
        return this.columns;
    }

    @Override
    public Column[] getPrimaryKeys() {
        return this.primaryKeys;
    }

    public Object[] applyExpression(Object[] objects) {
        if (this.additionalExpression.size() == 0) {
            return objects;
        }
        Map<String, Object> param = Maps.newHashMap();
        for (int i = 0, j = objects.length; i < j; i++) {
            param.put(this.columns[i].getColumnName(), objects[i]);
        }
        Object[] result = Arrays.copyOf(objects, this.columns.length);
        for (int i = 0, j = result.length; i < j; i++) {
            String columnName = this.columns[i].getColumnName();
            if (this.additionalExpression.contains(columnName)) {
                result[i] = this.additionalExpression.evaluate(columnName, param);
                param.put(columnName, result[i]);
            }
        }
        return result;
    }

    public AddSettingTableMetaData changePrimaryKey(Column[] keyColumns) {
        return new AddSettingTableMetaData(this.tableName, keyColumns, this.columns, this.additionalExpression);
    }
}
