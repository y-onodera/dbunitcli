package yo.dbunitcli.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.util.Map;

public class Export implements Command<ExportOption>{

    private static final Logger logger = LoggerFactory.getLogger(Export.class);

    public static void main(String[] args) throws Exception {
        new Export().exec(args);
    }

    @Override
    public ExportOption getOptions() {
        return new ExportOption();
    }

    @Override
    public ExportOption getOptions(Map<String, Object> param) {
        return new ExportOption(param);
    }

    @Override
    public void exec(ExportOption options) throws Exception {
        ComparableDataSet dataSet = options.targetDataSet();
        IDataSetWriter writer = options.writer();
        writer.open(options.getResultFileName());
        for (String tableName : dataSet.getTableNames()) {
            writer.write(dataSet.getTable(tableName));
        }
        writer.close();
    }

}
