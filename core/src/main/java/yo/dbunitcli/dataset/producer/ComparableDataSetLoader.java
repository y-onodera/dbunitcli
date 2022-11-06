package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.*;

import java.util.Map;
import java.util.stream.Stream;

public class ComparableDataSetLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Parameter parameter;

    public ComparableDataSetLoader(final Parameter parameter) {
        this.parameter = parameter;
    }

    public Stream<Map<String, Object>> loadParam(final ComparableDataSetParam param) throws DataSetException {
        return this.loadDataSet(param).toMap();
    }

    public ComparableDataSet loadDataSet(final ComparableDataSetParam param) {
        LOGGER.info("create DataSetLoader from {}", param);
        return new ComparableDataSetImpl(this.getComparableDataSetProducer(param));
    }

    protected ComparableDataSetProducer getComparableDataSetProducer(final ComparableDataSetParam param) {
        switch (param.getSource()) {
            case table:
                return new ComparableDBDataSetProducer(param);
            case sql:
                return new ComparableQueryDataSetProducer(param, this.parameter);
            case xlsx:
                return new ComparableXlsxDataSetProducer(param);
            case xls:
                return new ComparableXlsDataSetProducer(param);
            case csvq:
                return new ComparableCSVQueryDataSetProducer(param, this.parameter);
            case csv:
                return new ComparableCsvDataSetProducer(param);
            case reg:
                return new ComparableRegexSplitDataSetProducer(param);
            case fixed:
                return new ComparableFixedFileDataSetProducer(param);
            case file:
                return new ComparableFileDataSetProducer(param);
            case dir:
                return new ComparableDirectoryDataSetProducer(param);
        }
        throw new UnsupportedOperationException(param.getSource().name());
    }
}
