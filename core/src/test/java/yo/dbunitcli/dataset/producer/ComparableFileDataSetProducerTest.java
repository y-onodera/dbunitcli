package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;

import java.io.File;

public class ComparableFileDataSetProducerTest {

    @Test
    public void test() throws DataSetException {
        final File src = new File(".", "src/test/java");
        final ComparableDataSet actual = new ComparableFileDataSetProducer(
                ComparableDataSetParam.builder()
                        .setSrc(src)
                        .setSource(DataSourceType.file)
                        .setRegInclude("DataSetProducer")
                        .setRegExclude("Csv")
                        .setRecursive(true)
                        .build()).loadDataSet();
        Assertions.assertEquals(src.getPath(), actual.src());
        Assertions.assertEquals(1, actual.getTables().length);
        final ITable table = actual.getTable("java");
        Assertions.assertEquals(5, table.getRowCount());
    }
}