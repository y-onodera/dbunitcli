package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.Parameter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ComparableDataSetLoader {

    private final IDatabaseConnection connection;

    private final Parameter parameter;

    public ComparableDataSetLoader() {
        this(null, Parameter.none());
    }

    public ComparableDataSetLoader(Parameter parameter) {
        this(null, parameter);
    }

    public ComparableDataSetLoader(IDatabaseConnection connection) {
        this(connection, Parameter.none());
    }

    public ComparableDataSetLoader(IDatabaseConnection iDatabaseConnection, Parameter parameter) {
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
