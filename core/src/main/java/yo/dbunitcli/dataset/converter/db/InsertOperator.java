package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.OperationData;

import java.util.BitSet;
import java.util.stream.IntStream;

public class InsertOperator extends DBOperator {

    public InsertOperator(final IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected BitSet getIgnoreMapping(final Object[] row) throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        final BitSet ignoreMapping = new BitSet();
        IntStream.range(0, columns.length).forEach(i -> {
            final Column column = columns[i];
            final Object value = row[i];
            if (value == ITable.NO_VALUE || value == null && column.isNotNullable() && column.hasDefaultValue()) {
                ignoreMapping.set(i);
            }
        });
        return ignoreMapping;
    }

    @Override
    protected boolean equalsIgnoreMapping(final BitSet ignoreMapping, final Object[] row) throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            final boolean bit = ignoreMapping.get(i);
            final Object value = row[i];
            if (bit && value != ITable.NO_VALUE || !bit && value == ITable.NO_VALUE) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        final StringBuilder sqlBuffer = new StringBuilder(128);
        sqlBuffer.append("insert into ");
        sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
        sqlBuffer.append(" (");
        String columnSeparator = "";
        for (int i = 0; i < columns.length; ++i) {
            if (!this.ignoreMapping.get(i)) {
                final String columnName = this.getQualifiedName(null, columns[i].getColumnName(), this.connection);
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
