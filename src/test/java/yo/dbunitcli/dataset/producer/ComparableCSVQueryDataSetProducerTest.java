package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import yo.dbunitcli.application.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetLoaderParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.producer.ComparableCSVQueryDataSetProducer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ComparableCSVQueryDataSetProducerTest {

    private String resource;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.resource = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
    }

    @Test
    public void createDataSetFromFile() throws DataSetException {
        File src = new File(this.resource, "csvquery/singlefile/joinQuery.txt");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("joinQuery");
        Assert.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assert.assertEquals(2, actualTable.getRowCount());
        Assert.assertEquals("3", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("1", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assert.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assert.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assert.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assert.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assert.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }

    @Test
    public void createDataSetFromDirectoryContainsSingleFile() throws DataSetException {
        File src = new File(this.resource, "csvquery/singlefile");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("joinQuery");
        Assert.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assert.assertEquals(2, actualTable.getRowCount());
        Assert.assertEquals("3", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("1", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assert.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assert.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assert.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assert.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assert.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }

    @Test
    public void createDataSetFromDirectoryContainsMultiFile() throws DataSetException {
        File src = new File(this.resource, "csvquery/multifile");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(2, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("joinQuery");
        Assert.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assert.assertEquals(2, actualTable.getRowCount());
        Assert.assertEquals("3", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("1", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assert.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assert.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assert.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assert.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assert.assertEquals("10000", actualTable.getValue(1, "VALUE"));
        actualTable = actual.getTable("detailQuery");
        Assert.assertEquals("detailQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("VALUE1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("VALUE2"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("2", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("3", actualTable.getValue(2, "KEY"));
        Assert.assertEquals("100", actualTable.getValue(0, "VALUE1"));
        Assert.assertEquals("0", actualTable.getValue(1, "VALUE1"));
        Assert.assertEquals("1001", actualTable.getValue(2, "VALUE1"));
        Assert.assertEquals("200", actualTable.getValue(0, "VALUE2"));
        Assert.assertEquals("0", actualTable.getValue(1, "VALUE2"));
        Assert.assertEquals("2000", actualTable.getValue(2, "VALUE2"));
    }

    @Test
    public void createDataSetFromDirectoryOnlyIncludeFile() throws DataSetException {
        File src = new File(this.resource, "csvquery/multifile");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setRegInclude("detailQuery")
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("detailQuery");
        Assert.assertEquals("detailQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("VALUE1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("VALUE2"));
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("2", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("3", actualTable.getValue(2, "KEY"));
        Assert.assertEquals("100", actualTable.getValue(0, "VALUE1"));
        Assert.assertEquals("0", actualTable.getValue(1, "VALUE1"));
        Assert.assertEquals("1001", actualTable.getValue(2, "VALUE1"));
        Assert.assertEquals("200", actualTable.getValue(0, "VALUE2"));
        Assert.assertEquals("0", actualTable.getValue(1, "VALUE2"));
        Assert.assertEquals("2000", actualTable.getValue(2, "VALUE2"));
    }

    @Test
    public void createDataSetFromDirectoryFilterExcludeFile() throws DataSetException {
        File src = new File(this.resource, "csvquery/multifile");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setRegExclude("detailQuery")
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("joinQuery");
        Assert.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assert.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assert.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEY"));
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assert.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assert.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assert.assertEquals(2, actualTable.getRowCount());
        Assert.assertEquals("3", actualTable.getValue(0, "KEY"));
        Assert.assertEquals("1", actualTable.getValue(1, "KEY"));
        Assert.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assert.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assert.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assert.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assert.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assert.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }
}