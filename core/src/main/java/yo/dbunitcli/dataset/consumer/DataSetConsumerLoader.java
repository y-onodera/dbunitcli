package yo.dbunitcli.dataset.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConsumer;

public class DataSetConsumerLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    public IDataSetConsumer get(DataSetConsumerParam param) throws DataSetException {
        LOGGER.info("create DataSetWriter param:{}", param);
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
