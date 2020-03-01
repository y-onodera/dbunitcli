package yo.dbunitcli.application.setting;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.writer.*;

import java.io.File;

public class DataSetWriterLoader {

    private static Logger logger = LoggerFactory.getLogger(DataSetWriterLoader.class);

    public IDataSetWriter get(DatabaseConnectionLoader connectionLoader, String resultType, String operation, File resultDir, String outputEncoding) throws DataSetException {
        logger.info("create DataSetWriter type:{} DBOperation:{} resultDir:{} encoding:{}"
                , resultType, operation, resultDir, outputEncoding);
        if (DataSourceType.TABLE.isEqual(resultType)) {
            return new DBDataSetWriter(connectionLoader.loadConnection(), operation);
        } else if (DataSourceType.XLSX.isEqual(resultType)) {
            return new XlsxDataSetWriter(resultDir);
        } else if (DataSourceType.XLS.isEqual(resultType)) {
            return new XlsDataSetWriter(resultDir);
        } else if (DataSourceType.CSV.isEqual(resultType)) {
            return new CsvDataSetWriterWrapper(resultDir, outputEncoding);
        }
        throw new UnsupportedOperationException(resultType);
    }
}
