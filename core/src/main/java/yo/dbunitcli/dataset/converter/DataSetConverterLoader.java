package yo.dbunitcli.dataset.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConverter;

public class DataSetConverterLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetConverterLoader.class);

    public IDataSetConverter get(final DataSetConsumerParam param) {
        DataSetConverterLoader.LOGGER.info("create DataSetWriter param:{}", param);
        if (DataSourceType.table == param.resultType()) {
            return new DBConverter(param);
        } else if (DataSourceType.xlsx == param.resultType()) {
            return new XlsxConverter(param);
        } else if (DataSourceType.xls == param.resultType()) {
            return new XlsConverter(param);
        } else if (DataSourceType.csv == param.resultType()) {
            return new CsvConverter(param);
        }
        throw new UnsupportedOperationException(param.resultType().toString());
    }
}
