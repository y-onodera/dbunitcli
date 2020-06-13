package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ComparableXlsxDataSetProducerTest {

    private String resource;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.resource = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
    }

    @Test
    public void createDataSetFromFile() throws DataSetException {
        File src = new File(this.resource, "multifile.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));

        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(2, actual.getTableNames().length);
        ComparableTable actualTable = actual.getTable("multi1");
        Assert.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, "key"));
        Assert.assertEquals("2", actualTable.getValue(1, "key"));
        Assert.assertEquals("3", actualTable.getValue(2, "key"));
        Assert.assertEquals("2", actualTable.getValue(0, "column1"));
        Assert.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
        Assert.assertEquals("", actualTable.getValue(2, "column1"));
        Assert.assertEquals("3", actualTable.getValue(0, "column2"));
        Assert.assertEquals("test", actualTable.getValue(1, "column2"));
        Assert.assertEquals("", actualTable.getValue(2, "column2"));
        Assert.assertEquals("4", actualTable.getValue(0, "column3"));
        Assert.assertEquals("5", actualTable.getValue(1, "column3"));
        Assert.assertEquals("", actualTable.getValue(2, "column3"));
        actualTable = actual.getTable("multi2");
        Assert.assertEquals("multi2", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("columna"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("columnb"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("columnc"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, 0));
        Assert.assertEquals("2", actualTable.getValue(1, 0));
        Assert.assertEquals("3", actualTable.getValue(2, 0));
        Assert.assertEquals("2", actualTable.getValue(0, 1));
        Assert.assertEquals("あ\r\nいうえお", actualTable.getValue(1, 1));
        Assert.assertEquals("", actualTable.getValue(2, 1));
        Assert.assertEquals("3", actualTable.getValue(0, 2));
        Assert.assertEquals("test", actualTable.getValue(1, 2));
        Assert.assertEquals("", actualTable.getValue(2, 2));
        Assert.assertEquals("4", actualTable.getValue(0, 3));
        Assert.assertEquals("5", actualTable.getValue(1, 3));
        Assert.assertEquals("", actualTable.getValue(2, 3));
    }

    @Test
    public void createDataSetFromFileIncludeSheet() throws DataSetException {
        File src = new File(this.resource, "multifile.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegInclude("multi1")
                                .build()));

        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTableNames().length);
        ComparableTable actualTable = actual.getTable("multi1");
        Assert.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, "key"));
        Assert.assertEquals("2", actualTable.getValue(1, "key"));
        Assert.assertEquals("3", actualTable.getValue(2, "key"));
        Assert.assertEquals("2", actualTable.getValue(0, "column1"));
        Assert.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
        Assert.assertEquals("", actualTable.getValue(2, "column1"));
        Assert.assertEquals("3", actualTable.getValue(0, "column2"));
        Assert.assertEquals("test", actualTable.getValue(1, "column2"));
        Assert.assertEquals("", actualTable.getValue(2, "column2"));
        Assert.assertEquals("4", actualTable.getValue(0, "column3"));
        Assert.assertEquals("5", actualTable.getValue(1, "column3"));
        Assert.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromFileExcludeSheet() throws DataSetException {
        File src = new File(this.resource, "multifile.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegExclude("multi2")
                                .build()));

        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTableNames().length);
        ComparableTable actualTable = actual.getTable("multi1");
        Assert.assertEquals("multi1", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("key"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("column2"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("column3"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, "key"));
        Assert.assertEquals("2", actualTable.getValue(1, "key"));
        Assert.assertEquals("3", actualTable.getValue(2, "key"));
        Assert.assertEquals("2", actualTable.getValue(0, "column1"));
        Assert.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
        Assert.assertEquals("", actualTable.getValue(2, "column1"));
        Assert.assertEquals("3", actualTable.getValue(0, "column2"));
        Assert.assertEquals("test", actualTable.getValue(1, "column2"));
        Assert.assertEquals("", actualTable.getValue(2, "column2"));
        Assert.assertEquals("4", actualTable.getValue(0, "column3"));
        Assert.assertEquals("5", actualTable.getValue(1, "column3"));
        Assert.assertEquals("", actualTable.getValue(2, "column3"));
    }
}