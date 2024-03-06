package yo.dbunitcli.dataset.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

public class DataSetConverterLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetConverterLoader.class);

    public IDataSetConverter get(final DataSetConsumerParam param) {
        DataSetConverterLoader.LOGGER.info("create DataSetWriter param:{}", param);
        return switch (param.resultType()) {
            case table -> new DBConverter(param);
            case xlsx -> new XlsxConverter(param);
            case xls -> new XlsConverter(param);
            case csv -> new CsvConverter(param);
        };
    }
}
