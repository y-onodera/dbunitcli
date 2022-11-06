package yo.dbunitcli.dataset.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConverter;

public class DataSetConverterLoader {

    private static final Logger LOGGER = LogManager.getLogger();

    public IDataSetConverter get(final DataSetConsumerParam param) {
        LOGGER.info("create DataSetWriter param:{}", param);
        if (DataSourceType.table == param.getResultType()) {
            return new DBConverter(param);
        } else if (DataSourceType.xlsx == param.getResultType()) {
            return new XlsxConverter(param);
        } else if (DataSourceType.xls == param.getResultType()) {
            return new XlsConverter(param);
        } else if (DataSourceType.csv == param.getResultType()) {
            return new CsvConverter(param);
        }
        throw new UnsupportedOperationException(param.getResultType().toString());
    }
}
