package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.CompareSetting;

import java.io.File;

public class ComparableDataSetLoader {

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, String aSource, CompareSetting excludeColumns) throws DataSetException {
        switch (aSource) {
            case "xlsx":
                return new ComparableXlsxDataSet(aDir, excludeColumns);
            case "xls":
                return new ComparableXlsDataSet(aDir, excludeColumns);
            default:
                return new ComparableCSVDataSet(aDir, aEncoding, excludeColumns);
        }
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
