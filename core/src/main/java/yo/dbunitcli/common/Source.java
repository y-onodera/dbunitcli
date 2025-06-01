package yo.dbunitcli.common;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.Strings;

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

    public String[] toArray(final List<String> rowValues) {
        if (this.addFileInfo) {
            final List<String> result = new ArrayList<>(rowValues);
            result.set(rowValues.size() - 3, this.filePath);
            result.set(rowValues.size() - 2, this.fileName);
            result.set(rowValues.size() - 1, this.sheetName);
            return result.toArray(new String[0]);
        }
        return rowValues.toArray(new String[0]);
    }

    public String[] defaultColumnValues(final int originalColumnCount) {
        if (this.addFileInfo) {
            final String[] values = new String[originalColumnCount + TableMetaDataWithSource.OPTION_COLUMNS.length];
            Arrays.fill(values, "");
            values[values.length - 3] = this.filePath;
            values[values.length - 2] = this.fileName;
            values[values.length - 1] = this.sheetName;
            return values;
        }
        return new String[originalColumnCount];
    }

    public String targetName() {
        return Stream.of(this.tableName, this.sheetName, this.fileName)
                .filter(Strings::isNotEmpty)
                .findFirst()
                .orElseThrow();
    }
}
