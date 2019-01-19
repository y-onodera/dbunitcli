package example.y.onodera.dbunitcli;

import example.y.onodera.dbunitcli.dataset.ComparableCSVDataSet;
import example.y.onodera.dbunitcli.dataset.ComparableDataSet;
import example.y.onodera.dbunitcli.dataset.CompareResult;
import example.y.onodera.dbunitcli.dataset.DataSetCompareBuilder;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.dataset.DataSetException;
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
        CompareResult result = new DataSetCompareBuilder()
                .newDataSet(newData)
                .oldDataSet(oldData)
                .comparisonKeys(options.getComparisonKeys())
                .build()
                .exec();
        CsvDataSetWriter writer = new CsvDataSetWriter(options.getResultDir());
        if (options.getExpected() != null) {
            ComparableCSVDataSet expect = new ComparableCSVDataSet(options.getExpected());
            final ITable expectedTable = expect.getTables()[0];
            final String expectedTableName = expectedTable.getTableMetaData().getTableName();
            writer.write(result.toIDataSet(expectedTableName));
            Assertion.assertEquals(expectedTable, result.toITable(expectedTableName), new DefaultFailureHandler());
        } else {
            if (result.existDiff()) {
                writer.write(result.toIDataSet("COMPARE_RESULT"));
                System.exit(1);
            }
        }
    }

}
