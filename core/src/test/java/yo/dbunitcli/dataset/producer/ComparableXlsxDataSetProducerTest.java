package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ComparableXlsxDataSetProducerTest {

    private String resource;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.resource = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8")
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void createDataSetFromFile() throws DataSetException {
        final File src = new File(this.resource, "multifile.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));

        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(2, actual.getTableNames().length);
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
        Assertions.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
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
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("columna"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("columnb"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("columnc"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, 0));
        Assertions.assertEquals("2", actualTable.getValue(1, 0));
        Assertions.assertEquals("3", actualTable.getValue(2, 0));
        Assertions.assertEquals("2", actualTable.getValue(0, 1));
        Assertions.assertEquals("あ\r\nいうえお", actualTable.getValue(1, 1));
        Assertions.assertEquals("", actualTable.getValue(2, 1));
        Assertions.assertEquals("3", actualTable.getValue(0, 2));
        Assertions.assertEquals("test", actualTable.getValue(1, 2));
        Assertions.assertEquals("", actualTable.getValue(2, 2));
        Assertions.assertEquals("4", actualTable.getValue(0, 3));
        Assertions.assertEquals("5", actualTable.getValue(1, 3));
        Assertions.assertEquals("", actualTable.getValue(2, 3));
    }

    @Test
    public void createDataSetFromFileIncludeSheet() throws DataSetException {
        final File src = new File(this.resource, "multifile.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegTableInclude("multi1")
                                .build()));

        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTableNames().length);
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
        Assertions.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromFileExcludeSheet() throws DataSetException {
        final File src = new File(this.resource, "multifile.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegTableExclude("multi2")
                                .build()));

        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTableNames().length);
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
        Assertions.assertEquals("あ\r\nいうえお", actualTable.getValue(1, "column1"));
        Assertions.assertEquals("", actualTable.getValue(2, "column1"));
        Assertions.assertEquals("3", actualTable.getValue(0, "column2"));
        Assertions.assertEquals("test", actualTable.getValue(1, "column2"));
        Assertions.assertEquals("", actualTable.getValue(2, "column2"));
        Assertions.assertEquals("4", actualTable.getValue(0, "column3"));
        Assertions.assertEquals("5", actualTable.getValue(1, "column3"));
        Assertions.assertEquals("", actualTable.getValue(2, "column3"));
    }
}