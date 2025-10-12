package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record Source(String filePath, String fileName, String sheetName, String tableName, boolean addFileInfo) {
    public static Source NONE = new Source("", "", "", "", false);

    public Source(final File sourceFile, final boolean addFileInfo) {
        this(sourceFile.getAbsolutePath().replaceAll("\\\\", "/"), sourceFile.getName()
                , "", "", addFileInfo);
    }

    public TableMetaDataWithSource wrap(final ITableMetaData tableMetaData) {
        try {
            return new TableMetaDataWithSource(tableMetaData, this);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }


    public Source addFileInfo(final boolean addFileInfo) {
        return new Source(this.filePath, this.fileName, this.sheetName, this.tableName, addFileInfo);
    }

    public Source sheetName(final String sheetName) {
        return new Source(this.filePath, this.fileName, sheetName, this.tableName, this.addFileInfo);
    }

    public Source tableName(final String tableName) {
        return new Source(this.filePath, this.fileName, this.sheetName, tableName, this.addFileInfo);
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

}