package yo.dbunitcli.common;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record Source(String filePath, String fileName, String sheetName, String tableName, boolean addFileInfo,
                     boolean join) {
    public static Source NONE = new Source("", "", "", "", false, false);

    public static Source JOIN = new Source("", "", "", "", false, true);

    public Source(final File sourceFile, final boolean addFileInfo) {
        this(sourceFile.getAbsolutePath().replaceAll("\\\\", "/"), sourceFile.getName()
                , "", "", addFileInfo, false);
    }

    public TableMetaDataWithSource wrap(final ITableMetaData tableMetaData) {
        try {
            return new TableMetaDataWithSource(tableMetaData, this);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }


    public Source addFileInfo(final boolean addFileInfo) {
        return new Source(this.filePath, this.fileName, this.sheetName, this.tableName, addFileInfo, this.join);
    }

    public Source sheetName(final String sheetName) {
        return new Source(this.filePath, this.fileName, sheetName, this.tableName, this.addFileInfo, this.join);
    }

    public Source tableName(final String tableName) {
        return new Source(this.filePath, this.fileName, this.sheetName, tableName, this.addFileInfo, this.join);
    }

    public Column[] getColumns(final ITableMetaData tableMetaData) throws DataSetException {
        if (this.addFileInfo) {
            return Stream.concat(Arrays.stream(tableMetaData.getColumns()), Arrays.stream(TableMetaDataWithSource.OPTION_COLUMNS))
                    .toArray(Column[]::new);
        }
        return tableMetaData.getColumns();
    }

    public Object[] apply(final List<Object> rowValues) {
        if (this.addFileInfo) {
            final List<Object> result = new ArrayList<>(rowValues);
            result.add(this.filePath);
            result.add(this.fileName);
            result.add(this.sheetName);
            return result.toArray(new Object[0]);
        }
        return rowValues.toArray(new Object[0]);
    }

    public Object[] apply(final Object[] originalValue) {
        if (this.addFileInfo) {
            final Object[] result = Arrays.copyOf(originalValue, originalValue.length + 3);
            result[originalValue.length] = this.filePath;
            result[originalValue.length + 1] = this.fileName;
            result[originalValue.length + 2] = this.sheetName;
            return result;
        }
        return originalValue;
    }

    public String[] defaultColumnValues(final int columnCount) {
        final String[] values = new String[columnCount];
        Arrays.fill(values, "");
        if (this.addFileInfo) {
            values[values.length - 3] = this.filePath;
            values[values.length - 2] = this.fileName;
            values[values.length - 1] = this.sheetName;
        }
        return values;
    }

    public TableMetaDataWithSource createMetaData(final String[] header) {
        try {
            final String tableName = this.getTableName();
            return this.wrap(new DefaultTableMetaData(tableName,
                    Arrays.stream(header).map(s -> new Column(s.trim(), DataType.UNKNOWN)).toArray(Column[]::new)));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create metadata with file info", e);
        }
    }

    public String getTableName() {
        return this.tableName.isEmpty()
                ? this.fileName.substring(0, this.fileName.lastIndexOf("."))
                : this.tableName;
    }

    public TableMetaDataWithSource createMetaData(final Column[] columns) {
        try {
            final String tableName = this.getTableName();
            return this.wrap(new DefaultTableMetaData(tableName, columns));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create metadata with file info", e);
        }
    }

}