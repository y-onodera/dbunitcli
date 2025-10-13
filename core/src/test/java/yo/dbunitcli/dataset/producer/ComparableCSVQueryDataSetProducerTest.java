package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ComparableCSVQueryDataSetProducerTest {

    private String resource;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.resource = URLDecoder.decode(this.getClass().getResource(".").getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void createDataSetFromFile() throws DataSetException {
        final File src = new File(this.resource, "csvquery/singlefile/joinQuery.txt");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("joinQuery");
        Assertions.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assertions.assertEquals(2, actualTable.getRowCount());
        Assertions.assertEquals("3", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("1", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assertions.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assertions.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assertions.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assertions.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assertions.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }

    @Test
    public void createDataSetFromDirectoryContainsSingleFile() throws DataSetException {
        final File src = new File(this.resource, "csvquery/singlefile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("joinQuery");
        Assertions.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assertions.assertEquals(2, actualTable.getRowCount());
        Assertions.assertEquals("3", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("1", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assertions.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assertions.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assertions.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assertions.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assertions.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }

    @Test
    public void createDataSetFromDirectoryContainsMultiFile() throws DataSetException {
        final File src = new File(this.resource, "csvquery/multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(2, actual.getTables().length);
        ComparableTable actualTable = actual.getTable("joinQuery");
        Assertions.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assertions.assertEquals(2, actualTable.getRowCount());
        Assertions.assertEquals("3", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("1", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assertions.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assertions.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assertions.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assertions.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assertions.assertEquals("10000", actualTable.getValue(1, "VALUE"));
        actualTable = actual.getTable("detailQuery");
        Assertions.assertEquals("detailQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("VALUE1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("VALUE2"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("2", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("3", actualTable.getValue(2, "KEYCOLUMN"));
        Assertions.assertEquals("100", actualTable.getValue(0, "VALUE1"));
        Assertions.assertEquals("0", actualTable.getValue(1, "VALUE1"));
        Assertions.assertEquals("1001", actualTable.getValue(2, "VALUE1"));
        Assertions.assertEquals("200", actualTable.getValue(0, "VALUE2"));
        Assertions.assertEquals("0", actualTable.getValue(1, "VALUE2"));
        Assertions.assertEquals("2000", actualTable.getValue(2, "VALUE2"));
    }

    @Test
    public void createDataSetFromDirectoryOnlyIncludeFile() throws DataSetException {
        final File src = new File(this.resource, "csvquery/multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegInclude("detailQuery")
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("detailQuery");
        Assertions.assertEquals("detailQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("VALUE1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("VALUE2"));
        Assertions.assertEquals(3, actualTable.getRowCount());
        Assertions.assertEquals("1", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("2", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("3", actualTable.getValue(2, "KEYCOLUMN"));
        Assertions.assertEquals("100", actualTable.getValue(0, "VALUE1"));
        Assertions.assertEquals("0", actualTable.getValue(1, "VALUE1"));
        Assertions.assertEquals("1001", actualTable.getValue(2, "VALUE1"));
        Assertions.assertEquals("200", actualTable.getValue(0, "VALUE2"));
        Assertions.assertEquals("0", actualTable.getValue(1, "VALUE2"));
        Assertions.assertEquals("2000", actualTable.getValue(2, "VALUE2"));
    }

    @Test
    public void createDataSetFromDirectoryFilterExcludeFile() throws DataSetException {
        final File src = new File(this.resource, "csvquery/multifile");
        final ComparableDataSet actual = new ComparableDataSet(
                new ComparableCSVQueryDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegExclude("detailQuery")
                                .setEncoding("UTF8")
                                .build()
                        , Parameter.none()));
        Assertions.assertEquals(src.getPath(), actual.getSrc());
        Assertions.assertEquals(1, actual.getTables().length);
        final ComparableTable actualTable = actual.getTable("joinQuery");
        Assertions.assertEquals("joinQuery", actualTable.getTableMetaData().getTableName());
        Assertions.assertEquals(4, actualTable.getTableMetaData().getColumns().length);
        Assertions.assertEquals(0, actualTable.getTableMetaData().getColumnIndex("KEYCOLUMN"));
        Assertions.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("CD1"));
        Assertions.assertEquals(2, actualTable.getTableMetaData().getColumnIndex("NAME"));
        Assertions.assertEquals(3, actualTable.getTableMetaData().getColumnIndex("VALUE"));
        Assertions.assertEquals(2, actualTable.getRowCount());
        Assertions.assertEquals("3", actualTable.getValue(0, "KEYCOLUMN"));
        Assertions.assertEquals("1", actualTable.getValue(1, "KEYCOLUMN"));
        Assertions.assertEquals("C1", actualTable.getValue(0, "CD1"));
        Assertions.assertEquals("A1", actualTable.getValue(1, "CD1"));
        Assertions.assertEquals("花子", actualTable.getValue(0, "NAME"));
        Assertions.assertEquals("太郎", actualTable.getValue(1, "NAME"));
        Assertions.assertEquals("30000", actualTable.getValue(0, "VALUE"));
        Assertions.assertEquals("10000", actualTable.getValue(1, "VALUE"));
    }
}