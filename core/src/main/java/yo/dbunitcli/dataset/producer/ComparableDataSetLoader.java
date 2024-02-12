package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.*;

import java.util.Map;
import java.util.stream.Stream;

public class ComparableDataSetLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetLoader.class);

    private final Parameter parameter;

    public ComparableDataSetLoader(final Parameter parameter) {
        this.parameter = parameter;
    }

    public Stream<Map<String, Object>> loadParam(final ComparableDataSetParam param) {
        return this.loadDataSet(param).toMap();
    }

    public ComparableDataSet loadDataSet(final ComparableDataSetParam param) {
        ComparableDataSetLoader.LOGGER.info("create DataSetLoader from {}", param);
        return new ComparableDataSetImpl(this.getComparableDataSetProducer(param));
    }

    protected ComparableDataSetProducer getComparableDataSetProducer(final ComparableDataSetParam param) {
        switch (param.source()) {
            case table:
                return new ComparableDBDataSetProducer(param);
            case sql:
                return new ComparableQueryDataSetProducer(param, this.parameter);
            case xlsx:
                return new ComparableXlsxDataSetProducer(param);
            case xls:
                return new ComparableXlsDataSetProducer(param);
            case csv:
                return new ComparableCsvDataSetProducer(param);
            case csvq:
                return new ComparableCSVQueryDataSetProducer(param, this.parameter);
            case file:
                return new ComparableFileDataSetProducer(param);
            case dir:
                return new ComparableDirectoryDataSetProducer(param);
            case reg:
                return new ComparableRegexSplitDataSetProducer(param);
            case fixed:
                return new ComparableFixedFileDataSetProducer(param);
            case none:
                return new ComparableNoneDataSetProducer(param);
        }
        throw new UnsupportedOperationException(param.source().name());
    }
}
