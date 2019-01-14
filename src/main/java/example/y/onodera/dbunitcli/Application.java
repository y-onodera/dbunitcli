package example.y.onodera.dbunitcli;

import example.y.onodera.dbunitcli.dataset.ComparableCSVDataSet;
import example.y.onodera.dbunitcli.dataset.CompareResult;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.csv.CsvDataSetWriter;

public class Application {
    public static void main(String[] args) throws DatabaseUnitException {
        CommandLineOptions options = new CommandLineOptions();
        try {
            options.parse(args);
        } catch (Exception exp) {
            System.exit(2);
        }
        ComparableCSVDataSet oldData = new ComparableCSVDataSet(options.getOldDir(), options.getEncoding());
        ComparableCSVDataSet newData = new ComparableCSVDataSet(options.getNewDir(), options.getEncoding());
        CompareResult result = oldData.compare(newData, options.getComparisonKeys());
        CsvDataSetWriter writer = new CsvDataSetWriter(options.getResultDir());
        writer.write(result.toIDataSet());
        if (options.getExpected() != null) {
            ComparableCSVDataSet expect = new ComparableCSVDataSet(options.getExpected());
            Assertion.assertEquals(result.toITable(), expect.getTable("COMPARE_SCHEMA_RESULT"));
        } else {
            if (result.toITable().getRowCount() > 0) {
                System.exit(1);
            }
        }
    }
}
