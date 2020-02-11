package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Parameter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ComparableDataSetLoader {

    private static Logger logger = LoggerFactory.getLogger(ComparableDataSetLoader.class);
    private final IDatabaseConnection connection;

    private final Parameter parameter;

    public ComparableDataSetLoader() {
        this(null, Parameter.none());
    }

    public ComparableDataSetLoader(Parameter parameter) {
        this(null, parameter);
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
        logger.info("create DataSetLoader from {}", param);
        switch (param.getSource()) {
            case TABLE:
                return new ComparableDBDataSet(this.connection, param);
            case SQL:
                return new ComparableQueryDataSet(this.connection, this.parameter, param);
            case XLSX:
                return new ComparableXlsxDataSet(param);
            case XLS:
                return new ComparableXlsDataSet(param);
            case CSVQ:
                return new ComparableCSVQueryDataSet(param, this.parameter);
            case CSV:
                return new ComparableCSVDataSet(param);
            case REGSP:
                return new ComparableRegexSplitDataSet(param);
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
