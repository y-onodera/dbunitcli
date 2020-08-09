package yo.dbunitcli.application.setting;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetWriter;
import yo.dbunitcli.dataset.writer.CsvDataSetWriterWrapper;
import yo.dbunitcli.dataset.writer.DBDataSetWriter;
import yo.dbunitcli.dataset.writer.XlsDataSetWriter;
import yo.dbunitcli.dataset.writer.XlsxDataSetWriter;

public class DataSetWriterLoader {

    private static Logger logger = LoggerFactory.getLogger(DataSetWriterLoader.class);

    public IDataSetWriter get(DataSetWriterParam param) throws DataSetException {
        logger.info("create DataSetWriter param:{}", param);
        if (DataSourceType.TABLE.isEqual(param.getResultType())) {
            return new DBDataSetWriter(param);
        } else if (DataSourceType.XLSX.isEqual(param.getResultType())) {
            return new XlsxDataSetWriter(param);
        } else if (DataSourceType.XLS.isEqual(param.getResultType())) {
            return new XlsDataSetWriter(param);
        } else if (DataSourceType.CSV.isEqual(param.getResultType())) {
            return new CsvDataSetWriterWrapper(param);
        }
        throw new UnsupportedOperationException(param.getResultType());
    }
}
