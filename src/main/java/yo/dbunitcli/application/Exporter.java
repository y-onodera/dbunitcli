package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.io.File;

public class Exporter {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

    public static void main(String[] args) throws DataSetException {
        ExportOption options = new ExportOption();
        try {
            options.parse(args);
        } catch (Exception exp) {
            logger.error("option parse failed.", exp);
            System.exit(2);
        }
        ComparableDataSet dataSet = options.targetDataSet();
        IDataSetWriter writer = options.writer();
        String resultFile = "result";
        if (args[0].startsWith("@")) {
            resultFile = new File(args[0].replace("@", "")).getName();
            resultFile = resultFile.substring(0, resultFile.lastIndexOf("."));
        }
        writer.open(resultFile);
        for (String tableName : dataSet.getTableNames()) {
            writer.write(dataSet.getTable(tableName));
        }
        writer.close();
    }

}
