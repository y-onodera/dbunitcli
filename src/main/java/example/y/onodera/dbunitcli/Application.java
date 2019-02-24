package example.y.onodera.dbunitcli;

import example.y.onodera.dbunitcli.dataset.ComparableCSVDataSet;
import example.y.onodera.dbunitcli.dataset.ComparableDataSet;
import example.y.onodera.dbunitcli.compare.CompareResult;
import example.y.onodera.dbunitcli.compare.DataSetCompareBuilder;
import example.y.onodera.dbunitcli.dataset.CsvDataSetWriterWrapper;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSetWriter;

public class Application {
    public static void main(String[] args) throws DatabaseUnitException {
        CommandLineOptions options = new CommandLineOptions();
        try {
            options.parse(args);
        } catch (Exception exp) {
            System.exit(2);
        }
        ComparableDataSet oldData = options.oldDataSet();
        ComparableDataSet newData = options.newDataSet();
        CsvDataSetWriter writer = new CsvDataSetWriter(options.getResultDir());
        CompareResult result = new DataSetCompareBuilder()
                .newDataSet(newData)
                .oldDataSet(oldData)
                .comparisonKeys(options.getComparisonKeys())
                .dataSetWriter(new CsvDataSetWriterWrapper(writer))
                .build()
                .result();
        if (options.getExpected() != null) {
            ComparableCSVDataSet expect = new ComparableCSVDataSet(options.getExpected());
            final ITable expectedTable = expect.getTables()[0];
            final String expectedTableName = expectedTable.getTableMetaData().getTableName();
            Assertion.assertEquals(expectedTable, result.toITable(expectedTableName), new DefaultFailureHandler());
        } else {
            if (result.existDiff()) {
                System.exit(1);
            }
        }
    }

}