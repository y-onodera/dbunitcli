package yo.dbunitcli.dataset;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XlsDataSetWriter extends org.dbunit.dataset.excel.XlsDataSetWriter implements IDataSetWriter {

    private final File resultDir;

    private String filename;

    private DefaultDataSet dataSet;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public XlsDataSetWriter(File resultDir) {
        this.resultDir = resultDir;
    }

    @Override
    public void open(String aFileName) {
        this.filename = aFileName;
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
            this.write(this.dataSet, new FileOutputStream(new File(this.resultDir, getFilename())));
        } catch (IOException | DataSetException e) {
            throw new DataSetException(e);
        }
    }

    protected String getFilename() {
        return this.filename + ".xls";
    }

    @Override
    protected void setDateCell(Cell cell, Date value, Workbook workbook) {
        cell.setCellType(CellType.STRING);
        cell.setCellValue(sdf.format(value));
    }
}
