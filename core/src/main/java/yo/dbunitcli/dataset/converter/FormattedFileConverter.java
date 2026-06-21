package yo.dbunitcli.dataset.converter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.stream.IntStream;

public class FormattedFileConverter extends FlatFileConverter implements IDataSetConverter {

    private final String format;

    public FormattedFileConverter(final String theDirectory, final File resultDir, final String encoding,
                                  final boolean exportEmptyTable, final String format, final String extension) {
        super(theDirectory, resultDir, encoding, exportEmptyTable, false, extension != null ? extension : "txt");
        this.format = format;
    }

    public FormattedFileConverter(final DataSetConverterParam param) {
        this(param.resultDir().getAbsolutePath(), param.resultDir(), param.outputEncoding(), param.exportEmptyTable(),
             param.format(), param.extension());
    }

    @Override
    public IDataSetConverter split() {
        return new FormattedFileConverter(this.theDirectory, this.resultDir, this.encoding, this.exportEmptyTable,
                                          this.format, this.extension);
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        final Column[] columns = this.getColumns();
        this.write(String.format(this.format, IntStream.range(0, columns.length).mapToObj(i -> {
            final String columnName = columns[i].getColumnName();
            final Object value = values[i];
            if (value == null || value == ITable.NO_VALUE) {
                return "";
            }
            try {
                return DataType.asString(value);
            } catch (final TypeCastException e) {
                throw new AssertionError("table=" +
                                                 this.activeMetaData.getTableName() + ", row=" + i +
                                                 ", column=" + columnName +
                                                 ", value=" + value, e);
            }
        }).toArray(Object[]::new)));
        this.write(System.lineSeparator());
        this.writeRows++;
    }
}
