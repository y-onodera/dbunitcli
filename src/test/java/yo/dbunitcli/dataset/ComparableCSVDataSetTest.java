package yo.dbunitcli.dataset;


import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ComparableCSVDataSetTest {

    @Test
    public void createEmptyDataSetFromNoFileDirectory() throws DataSetException {
        ComparableCSVDataSet actual = new ComparableCSVDataSet(new File(this.getClass().getResource(".").getFile(), "nofile"), "windows-31j");
        Assert.assertEquals(actual.getTables().length, 0);
    }

    @Test
    public void createDataSetFromShiftJisFile() throws DataSetException {
        ComparableCSVDataSet actual = new ComparableCSVDataSet(new File(this.getClass().getResource(".").getFile(), "singlefile"), "windows-31j");
        Assert.assertEquals(1, actual.getTables().length);
        ITable actualTable = actual.getTables()[0];
        Assert.assertEquals("single", actualTable.getTableMetaData().getTableName());
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
        Assert.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
        Assert.assertEquals("", actualTable.getValue(2, "column1"));
        Assert.assertEquals("3", actualTable.getValue(0, "column2"));
        Assert.assertEquals("test", actualTable.getValue(1, "column2"));
        Assert.assertEquals("", actualTable.getValue(2, "column2"));
        Assert.assertEquals("4", actualTable.getValue(0, "column3"));
        Assert.assertEquals("5", actualTable.getValue(1, "column3"));
        Assert.assertEquals("", actualTable.getValue(2, "column3"));
    }

    @Test
    public void createDataSetFromUTF8File() throws DataSetException {
        ComparableCSVDataSet actual = new ComparableCSVDataSet(new File(this.getClass().getResource(".").getFile(), "multifile"), "UTF8");
        Assert.assertEquals(2, actual.getTables().length);
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
        Assert.assertEquals("あ\nいうえお", actualTable.getValue(1, "column1"));
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
        Assert.assertEquals(1, actualTable.getTableMetaData().getColumnIndex("column1"));
        Assert.assertEquals("", actualTable.getTableMetaData().getColumns()[2].getColumnName());
        Assert.assertEquals("", actualTable.getTableMetaData().getColumns()[3].getColumnName());
        Assert.assertEquals(3, actualTable.getRowCount());
        Assert.assertEquals("1", actualTable.getValue(0, 0));
        Assert.assertEquals("2", actualTable.getValue(1, 0));
        Assert.assertEquals("3", actualTable.getValue(2, 0));
        Assert.assertEquals("2", actualTable.getValue(0, 1));
        Assert.assertEquals("あ\nいうえお", actualTable.getValue(1, 1));
        Assert.assertEquals("", actualTable.getValue(2, 1));
        Assert.assertEquals("3", actualTable.getValue(0, 2));
        Assert.assertEquals("test", actualTable.getValue(1, 2));
        Assert.assertEquals("", actualTable.getValue(2, 2));
        Assert.assertEquals("4", actualTable.getValue(0, 3));
        Assert.assertEquals("5", actualTable.getValue(1, 3));
        Assert.assertEquals("", actualTable.getValue(2, 3));

    }
}