package yo.dbunitcli.application;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.compare.CompareResult;
import yo.dbunitcli.compare.DataSetCompareBuilder;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetLoader;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.util.Map;

public class Compare implements Command<CompareOption> {

    private static final Logger logger = LoggerFactory.getLogger(Compare.class);

    public static void main(String[] args) throws Exception {
        new Compare().exec(args);
    }

    @Override
    public CompareOption getOptions() {
        return new CompareOption();
    }

    @Override
    public CompareOption getOptions(Parameter param) {
        return new CompareOption(param);
    }

    @Override
    public void exec(CompareOption options) throws DatabaseUnitException {
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
            ComparableDataSet expect = new ComparableDataSetLoader().loadDataSet(options.getExpected(), options.getOutputEncoding());
            ITableIterator itr = expect.iterator();
            itr.next();
            final ITable expectedTable = itr.getTable();
            final String expectedTableName = expectedTable.getTableMetaData().getTableName();
            Assertion.assertEquals(expectedTable, result.toITable(expectedTableName), new DefaultFailureHandler());
        } else {
            if (result.existDiff()) {
                throw new AssertionError("unexpected diff found.");
            }
        }
        logger.info("compare success.");
    }
}