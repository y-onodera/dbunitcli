package yo.dbunitcli.application;

import yo.dbunitcli.dataset.*;
import yo.dbunitcli.compare.CompareResult;
import yo.dbunitcli.compare.DataSetCompareBuilder;
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
        IDataSetWriter writer = options.writer();
        CompareResult result = new DataSetCompareBuilder()
                .newDataSet(newData)
                .oldDataSet(oldData)
                .comparisonKeys(options.getComparisonKeys())
                .dataSetWriter(writer)
                .build()
                .result();
        if (options.getExpected() != null) {
            ComparableDataSet expect = new ComparableDataSetLoader().loadDataSet(options.getExpected());
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