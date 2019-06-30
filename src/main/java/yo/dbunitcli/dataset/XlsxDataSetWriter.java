package yo.dbunitcli.dataset;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.excel.XlsDataSetWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class XlsxDataSetWriter extends XlsDataSetWriter implements IDataSetWriter {

    private final File resultDir;

    private String filename;

    private DefaultDataSet dataSet;

    public XlsxDataSetWriter(File resultDir) {
        this.resultDir = resultDir;
    }

    @Override
    public void open(String aFileName) {
        this.filename = aFileName + ".xlsx";
        this.dataSet = new DefaultDataSet();
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        this.dataSet.addTable(new SortedTable(aTable));
    }

    @Override
    public void close() throws DataSetException {
        try {
            if (!this.resultDir.exists()) {
                this.resultDir.mkdirs();
            }
            this.write(this.dataSet, new FileOutputStream(new File(this.resultDir, this.filename)));
        } catch (IOException | DataSetException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    protected Workbook createWorkbook() {
        return new XSSFWorkbook();
    }
}
