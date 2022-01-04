package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;
import yo.dbunitcli.dataset.Parameter;

public class Convert implements Command<ConvertOption>{

    private static final Logger logger = LoggerFactory.getLogger(Convert.class);

    public static void main(String[] args) throws Exception {
        new Convert().exec(args);
    }

    @Override
    public ConvertOption getOptions() {
        return new ConvertOption();
    }

    @Override
    public ConvertOption getOptions(Parameter param) {
        return new ConvertOption(param);
    }

    @Override
    public void exec(ConvertOption options) throws Exception {
        ComparableDataSet dataSet = options.targetDataSet();
        IDataSetWriter writer = options.writer();
        writer.open(options.getResultPath());
        for (String tableName : dataSet.getTableNames()) {
            writer.write(dataSet.getTable(tableName));
        }
        writer.close();
    }

}
