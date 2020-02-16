package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.junit.Assert;
import org.junit.Test;
import yo.dbunitcli.application.Parameter;

import java.io.File;

public class ComparableCSVQueryDataSetTest {

    @Test
    public void createDataSetFromSingleFile() throws DataSetException {
        ComparableCSVQueryDataSet actual = new ComparableCSVQueryDataSet(ComparableDataSetLoaderParam
                .builder()
                .setSrc(new File(this.getClass().getResource("csvquery").getFile(), "singlefile"))
                .setEncoding("UTF8")
                .build(), Parameter.none());
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
    public void createDataSetFromMultiFile() throws DataSetException {
        ComparableCSVQueryDataSet actual = new ComparableCSVQueryDataSet(ComparableDataSetLoaderParam
                .builder()
                .setSrc(new File(this.getClass().getResource("csvquery").getFile(), "multifile"))
                .setEncoding("UTF8")
                .build(), Parameter.none());
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
}