package example.y.onodera.dbunitcli.dataset;

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
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class ComparableCsvDataSetProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableCsvDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private File[] src;
    private String encoding = System.getProperty("file.encoding");

    public ComparableCsvDataSetProducer(File srcDir, String encoding) throws DataSetException {
        if (!srcDir.isDirectory()) {
            throw new DataSetException("'" + srcDir + "' should be a directory");
        }
        this.src = srcDir.listFiles((file, s) -> s.endsWith(".csv"));
        this.encoding = encoding;
    }

    public ComparableCsvDataSetProducer(File aSrcFile) throws DataSetException {
        if (!aSrcFile.isFile()) {
            throw new DataSetException("'" + aSrcFile.toString() + "' should be a file");
        }
        this.src = new File[]{aSrcFile};
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produce() - start");

        this.consumer.startDataSet();
        for (File file : this.src) {
            try {
                this.produceFromFile(file);
            } catch (CsvParserException | DataSetException e) {
                throw new DataSetException("error producing dataSet for table '" + file.toString() + "'", e);
            }
        }
        this.consumer.endDataSet();
    }

    private void produceFromFile(File theDataFile) throws DataSetException, CsvParserException {
        logger.debug("produceFromFile(theDataFile={}) - start", theDataFile);

        try {
            List readData = new CsvParserImpl().parse(
                    new BufferedReader(new InputStreamReader(new FileInputStream(theDataFile), this.encoding))
                    , theDataFile.toString());
            List readColumns = ((List) readData.get(0));
            Column[] columns = new Column[readColumns.size()];

            for (int i = 0; i < readColumns.size(); i++) {
                String columnName = (String) readColumns.get(i);
                columnName = columnName.trim();
                columns[i] = new Column(columnName, DataType.UNKNOWN);
            }

            String tableName = theDataFile.getName().substring(0, theDataFile.getName().indexOf(".csv"));
            ITableMetaData metaData = new DefaultTableMetaData(tableName, columns);
            this.consumer.startTable(metaData);
            for (int i = 1; i < readData.size(); i++) {
                List rowList = (List) readData.get(i);
                Object[] row = rowList.toArray();
                for (int col = 0; col < row.length; col++) {
                    row[col] = row[col].equals(CsvDataSetWriter.NULL) ? null : row[col];
                }
                this.consumer.row(row);
            }
            this.consumer.endTable();
        } catch (PipelineException | IOException | IllegalInputCharacterException e) {
            throw new DataSetException(e);
        }
    }
}
