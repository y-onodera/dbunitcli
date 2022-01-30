package yo.dbunitcli.dataset.writer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetWriter;

public class DataSetWriterLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataSetWriterLoader.class);

    public IDataSetWriter get(DataSetWriterParam param) throws DataSetException {
        logger.info("create DataSetWriter param:{}", param);
        if (DataSourceType.table == param.getResultType()) {
            return new DBDataSetWriter(param);
        } else if (DataSourceType.xlsx == param.getResultType()) {
            return new XlsxDataSetWriter(param);
        } else if (DataSourceType.xls == param.getResultType()) {
            return new XlsDataSetWriter(param);
        } else if (DataSourceType.csv == param.getResultType()) {
            return new CsvDataSetWriterWrapper(param);
        }
        throw new UnsupportedOperationException(param.getResultType().toString());
    }
}
