package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.OperationData;

import java.util.BitSet;

public class InsertConsumer extends IDataSetOperationConsumer {

    public InsertConsumer(IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected BitSet getIgnoreMapping(Object[] row) throws DataSetException {
        Column[] columns = this.metaData.getColumns();
        BitSet ignoreMapping = new BitSet();

        for (int i = 0; i < columns.length; ++i) {
            Column column = columns[i];
            Object value = row[i];
            if (value == ITable.NO_VALUE || value == null && column.isNotNullable() && column.hasDefaultValue()) {
                ignoreMapping.set(i);
            }
        }

        return ignoreMapping;
    }

    @Override
    protected boolean equalsIgnoreMapping(BitSet ignoreMapping, Object[] row) throws DataSetException {
        Column[] columns = this.metaData.getColumns();

        for (int i = 0; i < columns.length; ++i) {
            boolean bit = ignoreMapping.get(i);
            Object value = row[i];
            if (bit && value != ITable.NO_VALUE || !bit && value == ITable.NO_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        Column[] columns = this.metaData.getColumns();
        StringBuilder sqlBuffer = new StringBuilder(128);
        sqlBuffer.append("insert into ");
        sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
        sqlBuffer.append(" (");
        String columnSeparator = "";

        for (int i = 0; i < columns.length; ++i) {
            if (!this.ignoreMapping.get(i)) {
                String columnName = this.getQualifiedName(null, columns[i].getColumnName(), this.connection);
                sqlBuffer.append(columnSeparator);
                sqlBuffer.append(columnName);
                columnSeparator = ", ";
            }
        }

        sqlBuffer.append(") values (");
        String valueSeparator = "";

        for (int i = 0; i < columns.length; ++i) {
            if (!this.ignoreMapping.get(i)) {
                sqlBuffer.append(valueSeparator);
                sqlBuffer.append("?");
                valueSeparator = ", ";
            }
        }

        sqlBuffer.append(")");
        return new OperationData(sqlBuffer.toString(), columns);
    }

}
