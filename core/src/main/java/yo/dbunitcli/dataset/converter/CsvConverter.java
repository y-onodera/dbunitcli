package yo.dbunitcli.dataset.converter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.stream.IntStream;

public class CsvConverter extends FlatFileConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvConverter.class);
    private static final String QUOTE = "\"";
    private static final String ESCAPE = "\\";

    public CsvConverter(final DataSetConverterParam param) {
        this(param.resultDir().getAbsolutePath()
                , param.resultDir()
                , param.outputEncoding()
                , param.exportEmptyTable()
                , param.exportHeader()
                , param.extension());
    }

    public CsvConverter(final File resultDir, final String encoding) {
        this(resultDir.getAbsolutePath(), resultDir, encoding, true, true, null);
    }

    public CsvConverter(final String theDirectory, final File resultDir, final String encoding,
                        final boolean exportEmptyTable, final boolean exportHeader) {
        this(theDirectory, resultDir, encoding, exportEmptyTable, exportHeader, null);
    }

    public CsvConverter(final String theDirectory, final File resultDir, final String encoding,
                        final boolean exportEmptyTable, final boolean exportHeader, final String extension) {
        super(theDirectory, resultDir, encoding, exportEmptyTable, exportHeader, extension != null ? extension : "csv");
    }

    @Override
    public IDataSetConverter split() {
        return new CsvConverter(this.theDirectory, this.resultDir, this.encoding, this.exportEmptyTable,
                                this.exportHeader, this.extension);
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
        final Column[] columns = this.getColumns();
        IntStream.range(0, columns.length).forEach(i -> {
            final String columnName = columns[i].getColumnName();
            final Object value = objects[i];
            if (value == null || value == ITable.NO_VALUE) {
                this.write("");
            } else {
                try {
                    this.write(this.quoted(DataType.asString(value)));
                } catch (final TypeCastException e) {
                    throw new AssertionError("table=" +
                                                     this.activeMetaData.getTableName() + ", row=" + i +
                                                     ", column=" + columnName +
                                                     ", value=" + value, e);
                }
            }
            if (i < columns.length - 1) {
                this.write(",");
            }
        });
        this.write(System.lineSeparator());
        this.writeRows++;
    }

    @Override
    protected void writeHeader() {
        this.writeColumnNames();
        this.write(System.lineSeparator());
    }

    protected void writeColumnNames() {
        CsvConverter.LOGGER.debug("writeColumnNames() - start");
        final Column[] columns = this.getColumns();
        IntStream.range(0, columns.length).forEach(i -> {
            this.write(this.quoted(columns[i].getColumnName()));
            if (i < columns.length - 1) {
                this.write(",");
            }
        });
    }

    protected String quoted(final String stringValue) {
        return "\"" + this.escape(stringValue) + "\"";
    }

    protected String escape(final String stringValue) {
        final char[] array = stringValue.toCharArray();
        final char testExport = CsvConverter.QUOTE.toCharArray()[0];
        final char escape = CsvConverter.ESCAPE.toCharArray()[0];
        final StringBuilder buffer = new StringBuilder();
        for (final char c : array) {
            if (c == testExport || c == escape) {
                buffer.append('\\');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }
}
