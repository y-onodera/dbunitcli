package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.CommandLineOptions;
import yo.dbunitcli.compare.CompareSetting;

import java.io.File;

public class ComparableDataSetLoader {

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, CommandLineOptions.DataSourceType aSource, CompareSetting excludeColumns) throws DataSetException {
        switch (aSource) {
            case XLSX:
                return new ComparableXlsxDataSet(aDir, excludeColumns);
            case XLS:
                return new ComparableXlsDataSet(aDir, excludeColumns);
            case CSVQ:
                return new ComparableCSVQueryDataSet(aDir, aEncoding, excludeColumns);
            case CSV:
                return new ComparableCSVDataSet(aDir, aEncoding, excludeColumns);
        }
        return null;
    }

    public ComparableDataSet loadDataSet(File file) throws DataSetException {
        final String fileName = file.getName();
        if(fileName.endsWith(".xlsx")){
            return new ComparableXlsxDataSet(file);
        }else if(fileName.endsWith(".xls")){
            return new ComparableXlsDataSet(file);
        }
        return new ComparableCSVDataSet(file);
    }
}
