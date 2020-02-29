package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Test;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetLoaderParam;
import yo.dbunitcli.dataset.producer.ComparableDirectoryDataSetProducer;

import java.io.File;

public class ComparableDirectoryDataSetProducerTest {

    @Test
    public void test() throws DataSetException {
        File src = new File(".", "src/test/java");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableDirectoryDataSetProducer(
                        ComparableDataSetLoaderParam.builder()
                                .setSrc(src)
                                .setRegInclude("yo[/\\\\]+dbunitcli[/\\\\]+")
                                .setRegExclude("application")
                                .build()));
        Assert.assertEquals(src.getPath(), actual.getSrc());
        Assert.assertEquals(1, actual.getTables().length);
        ITable table = actual.getTable("java");
        Assert.assertEquals(2, table.getRowCount());
    }
}