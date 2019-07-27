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

    public List<Map<String, Object>> loadParam(ComparableDataSetLoaderParam param) throws DataSetException {
        ComparableDataSet dataSet = this.loadDataSet(param);
        return dataSet.toMap();
    }

    public ComparableDataSet loadDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        switch (param.getSource()) {
            case TABLE:
                return new ComparableDBDataSet(this.connection, param.getSrc(), param.getEncoding(), param.getExcludeColumns());
            case SQL:
                return new ComparableQueryDataSet(this.connection, param.getSrc(), param.getEncoding(), param.getExcludeColumns(), this.parameter);
            case XLSX:
                return new ComparableXlsxDataSet(param.getSrc(), param.getExcludeColumns());
            case XLS:
                return new ComparableXlsDataSet(param.getSrc(), param.getExcludeColumns());
            case CSVQ:
                return new ComparableCSVQueryDataSet(param.getSrc(), param.getEncoding(), param.getExcludeColumns(), this.parameter);
            case CSV:
                return new ComparableCSVDataSet(param.getSrc(), param.getEncoding(), param.getExcludeColumns());
            case REGSP:
                return new ComparableRegexSplitDataSet(param.getHeaderSplitPattern(), param.getDataSplitPattern(), param.getSrc(), param.getEncoding(), param.getExcludeColumns());
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
