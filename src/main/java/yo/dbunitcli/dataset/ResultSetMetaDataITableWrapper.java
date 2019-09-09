package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ResultSetMetaDataITableWrapper implements ITable {

    private final ITableMetaData tableMetaData;

    private ITable delegate;

    public ResultSetMetaDataITableWrapper(IDatabaseConnection conn, ITable target) throws SQLException, DataSetException {
        this.delegate = target;
        ITableMetaData metaData = target.getTableMetaData();
        try (Statement stmt = conn.getConnection().createStatement();
             ResultSet resultSet = stmt.executeQuery(this.toQuery(metaData))
        ) {
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            Column[] columns = new Column[rsMetaData.getColumnCount()];

            for (int i = 0; i < columns.length; ++i) {
                int rsIndex = i + 1;
                int columnType = rsMetaData.getColumnType(rsIndex);
                String columnTypeName = rsMetaData.getColumnTypeName(rsIndex);
                String columnName = rsMetaData.getColumnLabel(rsIndex);
                int isNullable = rsMetaData.isNullable(rsIndex);
                DataType dataType = new Oracle10DataTypeFactory().createDataType(columnType, columnTypeName, metaData.getTableName(), columnName);
                columns[i] = new Column(columnName, dataType, columnTypeName, Column.nullableValue(isNullable));
            }
            this.tableMetaData = new DefaultTableMetaData(metaData.getTableName(), columns);
        }
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return this.tableMetaData;
    }

    @Override
    public int getRowCount() {
        return this.delegate.getRowCount();
    }

    @Override
    public Object getValue(int i, String s) throws DataSetException {
        return this.delegate.getValue(i, s);
    }

    private static String toQuery(ITableMetaData metaData) throws DataSetException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        int i = 0;
        for (Column column : metaData.getColumns()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(column.getColumnName());
            i++;
        }
        return sb.append(" FROM ").append(metaData.getTableName()).append(";").toString();
    }
}
