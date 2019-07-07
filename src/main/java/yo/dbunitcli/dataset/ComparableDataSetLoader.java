package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.DataSourceType;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableDataSetLoader {

    private final IDatabaseConnection connection;

    public ComparableDataSetLoader() {
        this(null);
    }

    public ComparableDataSetLoader(IDatabaseConnection connection) {
        this.connection = connection;
    }

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, DataSourceType aSource, ColumnSetting excludeColumns) throws DataSetException {
        switch (aSource) {
            case TABLE:
                return new ComparableDBDataSet(this.connection, aDir, aEncoding, excludeColumns);
            case SQL:
                return new ComparableQueryDataSet(this.connection, aDir, aEncoding, excludeColumns);
            case XLSX:
                return new ComparableXlsxDataSet(aDir, excludeColumns);
            case XLS:
                return new ComparableXlsDataSet(aDir, excludeColumns);
            case CSVQ:
                return new ComparableCSVQueryDataSet(aDir, aEncoding, excludeColumns);
            case CSV:
                return new ComparableCSVDataSet(aDir, aEncoding, excludeColumns);
        }
        return null;
    }

    public ComparableDataSet loadDataSet(File file, String encoding) throws DataSetException {
        final String fileName = file.getName();
        if (fileName.endsWith(".xlsx")) {
            return new ComparableXlsxDataSet(file);
        } else if (fileName.endsWith(".xls")) {
            return new ComparableXlsDataSet(file);
        }
        return new ComparableCSVDataSet(file, encoding);
    }

}
