package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Test;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.io.File;

public class ComparableFileDataSetProducerTest {

    @Test
    public void test() throws DataSetException {
        File src = new File(".", "src/test/java");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableFileDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setRegInclude("DataSetProducer")
                                .setRegExclude("Csv")
                                .build()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ITable table = actual.getTable("java");
        Assert.assertEquals(5, table.getRowCount());
    }
}