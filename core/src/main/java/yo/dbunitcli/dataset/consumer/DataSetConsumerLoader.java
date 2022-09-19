package yo.dbunitcli.dataset.consumer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConsumer;

public class DataSetConsumerLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataSetConsumerLoader.class);

    public IDataSetConsumer get(DataSetConsumerParam param) throws DataSetException {
        logger.info("create DataSetWriter param:{}", param);
        if (DataSourceType.table == param.getResultType()) {
            return new DBConsumer(param);
        } else if (DataSourceType.xlsx == param.getResultType()) {
            return new XlsxConsumer(param);
        } else if (DataSourceType.xls == param.getResultType()) {
            return new XlsConsumer(param);
        } else if (DataSourceType.csv == param.getResultType()) {
            return new CsvConsumer(param);
        }
        throw new UnsupportedOperationException(param.getResultType().toString());
    }
}
