package yo.dbunitcli.application;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.producer.*;

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
        return this.loadDataSet(param).toMap();
    }

    public ComparableDataSet loadDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        logger.info("create DataSetLoader from {}", param);
        switch (param.getSource()) {
            case TABLE:
                return new ComparableDataSetImpl(new ComparableDBDataSetProducer(this.connection, param));
            case SQL:
                return new ComparableDataSetImpl(new ComparableQueryDataSetProducer(this.connection, param, this.parameter));
            case XLSX:
                return new ComparableDataSetImpl(new ComparableXlsxDataSetProducer(param));
            case XLS:
                return new ComparableDataSetImpl(new ComparableXlsDataSetProducer(param));
            case CSVQ:
                return new ComparableDataSetImpl(new ComparableCSVQueryDataSetProducer(param, this.parameter));
            case CSV:
                return new ComparableDataSetImpl(new ComparableCsvDataSetProducer(param));
            case REGSP:
                return new ComparableDataSetImpl(new ComparableRegexSplitDataSetProducer(param));
            case FILE:
                return new ComparableDataSetImpl(new ComparableFileDataSetProducer(param));
            case DIR:
                return new ComparableDataSetImpl(new ComparableDirectoryDataSetProducer(param));
        }
        return null;
    }
}
