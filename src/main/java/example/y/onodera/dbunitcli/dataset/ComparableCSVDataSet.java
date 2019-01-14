package example.y.onodera.dbunitcli.dataset;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
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
import java.util.Map;
import java.util.Set;

public class ComparableCSVDataSet extends CachedDataSet {
    private static final Logger logger = LoggerFactory.getLogger(ComparableCSVDataSet.class);

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new IDataSetProducer() {
            private IDataSetConsumer consumer = new DefaultConsumer();
            private File srcDir = aSrcDir;
            private String encoding = aEncoding;

            @Override
            public void setConsumer(IDataSetConsumer aConsumer) throws DataSetException {
                consumer = aConsumer;
            }

            @Override
            public void produce() throws DataSetException {
                logger.debug("produce() - start");

                if (!this.srcDir.isDirectory()) {
                    throw new DataSetException("'" + this.srcDir + "' should be a directory");
                }

                this.consumer.startDataSet();
                for (File file : this.srcDir.listFiles((file, s) -> s.endsWith(".csv"))) {
                    try {
                        this.produceFromFile(file, this.encoding);
                    } catch (CsvParserException | DataSetException e) {
                        throw new DataSetException("error producing dataset for table '" + file.toString() + "'", e);
                    }
                }
                this.consumer.endDataSet();
            }

            private void produceFromFile(File theDataFile, String aEncoding) throws DataSetException, CsvParserException {
                logger.debug("produceFromFile(theDataFile={}) - start", theDataFile);

                try {
                    List readData = new CsvParserImpl().parse(
                            new BufferedReader(new InputStreamReader(new FileInputStream(theDataFile), aEncoding))
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
        });
        this.srcDir = aSrcDir.getAbsolutePath();
    }

    public ComparableCSVDataSet(File aSrcFile) throws DataSetException {
        super(new IDataSetProducer() {
            private IDataSetConsumer consumer = new DefaultConsumer();

            @Override
            public void setConsumer(IDataSetConsumer aConsumer) {
                consumer = aConsumer;
            }

            @Override
            public void produce() throws DataSetException {
                logger.debug("produce() - start");
                this.consumer.startDataSet();
                try {
                    this.produceFromFile(aSrcFile, "UTF-8");
                } catch (CsvParserException | DataSetException e) {
                    throw new DataSetException("error producing dataset for table '" + aSrcFile.toString() + "'", e);
                }
                this.consumer.endDataSet();
            }

            private void produceFromFile(File theDataFile, String aEncoding) throws DataSetException, CsvParserException {
                logger.debug("produceFromFile(theDataFile={}) - start", theDataFile);

                try {
                    List readData = new CsvParserImpl().parse(
                            new BufferedReader(new InputStreamReader(new FileInputStream(theDataFile), aEncoding))
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
        });
        this.srcDir = aSrcFile.getParent();
    }

    public String getSrcDir() {
        return srcDir;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        return new ComparableTable(super.getTable(tableName));
    }

    public CompareResult compare(ComparableCSVDataSet newData, Map<String, List<String>> comparisonKeys) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        Set<String> oldTables = Sets.newHashSet(this.getTableNames());
        Set<String> newTables = Sets.newHashSet(newData.getTableNames());
        results.addAll(CompareDiff.deleteTable(Sets.filter(oldTables, Predicates.not(Predicates.in(newTables)))));
        results.addAll(CompareDiff.addTable(Sets.filter(newTables, Predicates.not(Predicates.in(oldTables)))));
        for (String tableName : Sets.intersection(oldTables, newTables)) {
            ComparableTable oldTable = this.getTable(tableName);
            ComparableTable newTable = newData.getTable(tableName);
            results.addAll(oldTable.compareSchema(newTable, comparisonKeys));
        }
        return new CompareResult(this.getSrcDir(), newData.getSrcDir(), results);
    }
}
