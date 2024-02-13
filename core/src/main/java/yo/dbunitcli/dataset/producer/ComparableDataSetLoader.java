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
        return switch (param.source()) {
            case table -> new ComparableDBDataSetProducer(param);
            case sql -> new ComparableQueryDataSetProducer(param, this.parameter);
            case xlsx -> new ComparableXlsxDataSetProducer(param);
            case xls -> new ComparableXlsDataSetProducer(param);
            case csv -> new ComparableCsvDataSetProducer(param);
            case csvq -> new ComparableCSVQueryDataSetProducer(param, this.parameter);
            case file -> new ComparableFileDataSetProducer(param);
            case dir -> new ComparableDirectoryDataSetProducer(param);
            case reg -> new ComparableRegexSplitDataSetProducer(param);
            case fixed -> new ComparableFixedFileDataSetProducer(param);
            case none -> new ComparableNoneDataSetProducer(param);
        };
    }
}
