package yo.dbunitcli.dataset.producer;


import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.DataSourceType;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ComparableCsvDataSetProducerTest {

    private String resource;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.resource = URLDecoder.decode(this.getClass().getResource(".").getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void createEmptyDataSetFromNoFileDirectory() throws DataSetException {
        final File src = new File(this.resource, "nofile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("windows-31j")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(0, actual.getTables().length);
    }

    @Test
    public void createDataSetFromDirectoryContainsShiftJisFile() throws DataSetException {
        final File src = new File(this.resource, "singlefile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("windows-31j")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ITable actualTable = actual.getTables()[0];
        Assertions.assertEquals("single", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "key"));
        Assertions.assertEquals("2", actualTable.getValue(1, "key"));
        Assertions.assertEquals("3", actualTable.getValue(2, "key"));
        Assertions.assertEquals("2", actualTable.getValue(0, "column1"));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromDirectoryContainsUTF8File() throws DataSetException {
        final File src = new File(this.resource, "multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(2, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("multi1");
        Assertions.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "key"));
        Assertions.assertEquals("2", actualTable.getValue(1, "key"));
        Assertions.assertEquals("3", actualTable.getValue(2, "key"));
        Assertions.assertEquals("2", actualTable.getValue(0, "column1"));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
        actualTable = actual.getTable("multi2");
        Assertions.assertEquals("multi2", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals("", actualTable.getTableMetaData().getColumns()[2].getColumnName());
        Assertions.assertEquals("", actualTable.getTableMetaData().getColumns()[3].getColumnName());
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, 0));
        Assertions.assertEquals("2", actualTable.getValue(1, 0));
        Assertions.assertEquals("3", actualTable.getValue(2, 0));
        Assertions.assertEquals("2", actualTable.getValue(0, 1));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, 1));
        Assertions.assertEquals("", actualTable.getValue(2, 1));
        Assertions.assertEquals("3", actualTable.getValue(0, 2));
        Assertions.assertEquals("test", actualTable.getValue(1, 2));
        Assertions.assertEquals("", actualTable.getValue(2, 2));
        Assertions.assertEquals("4", actualTable.getValue(0, 3));
        Assertions.assertEquals("5", actualTable.getValue(1, 3));
        Assertions.assertEquals("", actualTable.getValue(2, 3));
    }

    @Test
    public void createDataSetFromDirectoryIncludeFile() throws DataSetException {
        final File src = new File(this.resource, "multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .setRegInclude("multi1")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("multi1");
        Assertions.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "key"));
        Assertions.assertEquals("2", actualTable.getValue(1, "key"));
        Assertions.assertEquals("3", actualTable.getValue(2, "key"));
        Assertions.assertEquals("2", actualTable.getValue(0, "column1"));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromDirectoryExcludeFile() throws DataSetException {
        final File src = new File(this.resource, "multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .setRegExclude("multi2")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("multi1");
        Assertions.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "key"));
        Assertions.assertEquals("2", actualTable.getValue(1, "key"));
        Assertions.assertEquals("3", actualTable.getValue(2, "key"));
        Assertions.assertEquals("2", actualTable.getValue(0, "column1"));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromFile() throws DataSetException {
        final File src = new File(this.resource, "multifile/multi1.csv");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF8")
                                .build()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("multi1");
        Assertions.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "key"));
        Assertions.assertEquals("2", actualTable.getValue(1, "key"));
        Assertions.assertEquals("3", actualTable.getValue(2, "key"));
        Assertions.assertEquals("2", actualTable.getValue(0, "column1"));
        Assertions.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }
}