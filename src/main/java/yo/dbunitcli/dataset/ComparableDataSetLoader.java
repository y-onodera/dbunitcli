package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.DataSourceType;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ComparableDataSetLoader {

    private final IDatabaseConnection connection;

    private final Map<String, Object> parameter;

    public ComparableDataSetLoader() {
        this(null, Maps.newHashMap());
    }

    public ComparableDataSetLoader(Map<String, Object> parameter) {
        this(null, parameter);
    }

    public ComparableDataSetLoader(IDatabaseConnection connection) {
        this(connection, Maps.newHashMap());
    }

    public ComparableDataSetLoader(IDatabaseConnection iDatabaseConnection, Map<String, Object> parameter) {
        this.connection = iDatabaseConnection;
        this.parameter = parameter;
    }

    public List<Map<String, Object>> loadParam(File aFile, String aEncoding, DataSourceType aSource) throws DataSetException {
        ComparableDataSet dataset = this.loadDataSet(aFile, aEncoding, aSource, ColumnSetting.builder().build());
        return dataset.toMap();
    }

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, DataSourceType aSource, ColumnSetting excludeColumns) throws DataSetException {
        switch (aSource) {
            case TABLE:
                return new ComparableDBDataSet(this.connection, aDir, aEncoding, excludeColumns);
            case SQL:
                return new ComparableQueryDataSet(this.connection, aDir, aEncoding, excludeColumns, this.parameter);
            case XLSX:
                return new ComparableXlsxDataSet(aDir, excludeColumns);
            case XLS:
                return new ComparableXlsDataSet(aDir, excludeColumns);
            case CSVQ:
                return new ComparableCSVQueryDataSet(aDir, aEncoding, excludeColumns, this.parameter);
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
