package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

public record ComparableDataSetLoader(Parameter parameter) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetLoader.class);

    public ComparableDataSet loadDataSet(final ComparableDataSetParam param) {
        ComparableDataSetLoader.LOGGER.info("create DataSetLoader from {}", param);
        return this.getComparableDataSetProducer(param).loadDataSet();
    }

    public ComparableDataSetProducer getComparableDataSetProducer(final ComparableDataSetParam param) {
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
