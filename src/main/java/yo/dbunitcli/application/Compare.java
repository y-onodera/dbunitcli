package yo.dbunitcli.application;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.compare.CompareResult;
import yo.dbunitcli.compare.DataSetCompareBuilder;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetLoader;
import yo.dbunitcli.dataset.IDataSetWriter;

public class Compare {

    private static final Logger logger = LoggerFactory.getLogger(Compare.class);

    public static void main(String[] args) throws DatabaseUnitException {
        CompareOption options = new CompareOption();
        try {
            options.parse(args);
        } catch (Exception exp) {
            logger.error("option parse failed.", exp);
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
                logger.info("unexpected diff found.");
                System.exit(1);
            }
        }
        logger.info("compare success.");
    }
}