package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.PipelineException;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.csv.CsvParserImpl;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetLoaderParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.*;
import java.util.List;

public class ComparableCsvDataSetProducer implements ComparableDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableCsvDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final String encoding;
    private final TableNameFilter filter;
    private final ComparableDataSetLoaderParam param;

    public ComparableCsvDataSetProducer(ComparableDataSetLoaderParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles((file, s) -> s.endsWith(".csv"));
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.encoding = this.param.getEncoding();
        this.filter = this.param.getTableNameFilter();
    }

    @Override
    public ComparableDataSetLoaderParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath())) {
                try {
                    this.produceFromFile(file);
                } catch (CsvParserException | DataSetException e) {
                    throw new DataSetException("error producing dataSet for table '" + file.toString() + "'", e);
                }
            }
        }
        this.consumer.endDataSet();
    }

    private void produceFromFile(File theDataFile) throws DataSetException, CsvParserException {
        logger.info("produceFromFile(theDataFile={}) - start", theDataFile);
        try {
            List<List<Object>> readData = new CsvParserImpl().parse(
                    new BufferedReader(new InputStreamReader(new FileInputStream(theDataFile), this.encoding))
                    , theDataFile.toString());
            ITableMetaData metaData = this.createTableMetaData(theDataFile, readData.get(0));
            this.consumer.startTable(metaData);
            for (int i = 1; i < readData.size(); i++) {
                this.consumer.row(this.loadData(readData, i));
            }
            this.consumer.endTable();
        } catch (PipelineException | IOException | IllegalInputCharacterException e) {
            throw new DataSetException(e);
        }
    }

    protected Object[] loadData(List<List<Object>> readData, int i) {
        List<Object> rowList = readData.get(i);
        Object[] row = rowList.toArray();
        for (int col = 0; col < row.length; col++) {
            row[col] = row[col].equals(CsvDataSetWriter.NULL) ? null : row[col];
        }
        return row;
    }

    protected ITableMetaData createTableMetaData(File theDataFile, List<Object> readData) {
        Column[] columns = this.loadColumns(readData);
        String tableName = theDataFile.getName().substring(0, theDataFile.getName().indexOf(".csv"));
        return new DefaultTableMetaData(tableName, columns);
    }

    protected Column[] loadColumns(List<Object> readColumns) {
        Column[] columns = new Column[readColumns.size()];
        for (int i = 0; i < readColumns.size(); i++) {
            String columnName = (String) readColumns.get(i);
            columnName = columnName.trim();
            columns[i] = new Column(columnName, DataType.UNKNOWN);
        }
        return columns;
    }
}