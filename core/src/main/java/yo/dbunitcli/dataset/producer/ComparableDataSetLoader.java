package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.*;

import java.util.List;
import java.util.Map;

public class ComparableDataSetLoader {

    private static final Logger logger = LoggerFactory.getLogger(ComparableDataSetLoader.class);

    private final Parameter parameter;

    public ComparableDataSetLoader(Parameter parameter) {
        this.parameter = parameter;
    }

    public List<Map<String, Object>> loadParam(ComparableDataSetParam param) throws DataSetException {
        return this.loadDataSet(param).toMap();
    }

    public ComparableDataSet loadDataSet(ComparableDataSetParam param) throws DataSetException {
        logger.info("create DataSetLoader from {}", param);
        return new ComparableDataSetImpl(getComparableDataSetProducer(param));
    }

    private ComparableDataSetProducer getComparableDataSetProducer(ComparableDataSetParam param) throws DataSetException {
        switch (param.getSource()) {
            case TABLE:
                return new ComparableDBDataSetProducer(param);
            case SQL:
                return new ComparableQueryDataSetProducer(param, this.parameter);
            case XLSX:
                return new ComparableXlsxDataSetProducer(param);
            case XLS:
                return new ComparableXlsDataSetProducer(param);
            case CSVQ:
                return new ComparableCSVQueryDataSetProducer(param, this.parameter);
            case CSV:
                return new ComparableCsvDataSetProducer(param);
            case REGSP:
                return new ComparableRegexSplitDataSetProducer(param);
            case FIXED:
                return new ComparableFixedFileDataSetProducer(param);
            case FILE:
                return new ComparableFileDataSetProducer(param);
            case DIR:
                return new ComparableDirectoryDataSetProducer(param);
        }
        throw new UnsupportedOperationException(param.getSource().name());
    }
}
