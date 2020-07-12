package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.compare.CompareResult;
import yo.dbunitcli.compare.DataSetCompareBuilder;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.writer.IDataSetWriter;

import java.io.File;

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
        IDataSetWriter writer = options.writer(options.getResultFile());
        CompareResult result = new DataSetCompareBuilder()
                .newDataSet(newData)
                .oldDataSet(oldData)
                .comparisonKeys(options.getComparisonKeys())
                .dataSetWriter(writer)
                .build()
                .result();
        if (options.getExpected() != null) {
            if (new DataSetCompareBuilder()
                    .newDataSet(options.resultDataSet())
                    .oldDataSet(options.expectDataSet())
                    .comparisonKeys(options.getExpectedComparisonKeys())
                    .dataSetWriter(options.expectedDiffWriter())
                    .build()
                    .result().existDiff()) {
                throw new AssertionError("unexpected diff found.");
            }
        } else {
            if (result.existDiff()) {
                throw new AssertionError("unexpected diff found.");
            }
        }
        logger.info("compare success.");
    }
}