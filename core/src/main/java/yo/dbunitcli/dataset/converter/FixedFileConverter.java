package yo.dbunitcli.dataset.converter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import yo.dbunitcli.application.json.FromJsonFixedColumnDefBuilder;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FixedFileConverter extends FlatFileConverter implements IDataSetConverter {

    private final List<FixedColumnDef> columnDefs;
    private final LengthType lengthType;
    private Map<String, Integer> columnIndexByName;

    public FixedFileConverter(final String theDirectory, final File resultDir, final String encoding,
                              final boolean exportEmptyTable, final List<FixedColumnDef> columnDefs,
                              final LengthType lengthType) {
        super(theDirectory, resultDir, encoding, exportEmptyTable, false);
        this.columnDefs = columnDefs;
        this.lengthType = lengthType;
    }

    public FixedFileConverter(final DataSetConverterParam param) {
        this(param.resultDir().getAbsolutePath(), param.resultDir(), param.outputEncoding(), param.exportEmptyTable(),
             new FromJsonFixedColumnDefBuilder().build(new File(param.fixedColumnDefFile())),
             LengthType.of(param.fixedLengthType()));
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        super.startTable(metaData);
        this.columnIndexByName = this.buildColumnIndex(metaData);
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        super.reStartTable(tableMetaData, writeRows);
        this.columnIndexByName = tableMetaData.columnIndexes();
    }

    @Override
    protected String getExtension() {
        return "txt";
    }

    @Override
    public IDataSetConverter split() {
        return new FixedFileConverter(this.theDirectory, this.resultDir, this.encoding, this.exportEmptyTable,
                                      this.columnDefs, this.lengthType);
    }

    @Override
    public void row(final Object[] values) throws DataSetException {
        final StringBuilder sb = new StringBuilder();
        for (final FixedColumnDef def : this.columnDefs) {
            sb.append(this.pad(this.valueAt(values, def.name()), def));
        }
        this.write(sb.toString());
        this.write(System.lineSeparator());
        this.writeRows++;
    }

    private Map<String, Integer> buildColumnIndex(final ITableMetaData metaData) {
        Column[] columns;
        try {
            columns = metaData.getColumns();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
        return IntStream.range(0, columns.length)
                        .boxed()
                        .collect(Collectors.toMap(
                                it -> columns[it].getColumnName().toLowerCase()
                                , it -> it));
    }

    private String valueAt(final Object[] values, final String name) {
        final Integer idx = this.columnIndexByName.get(name.toLowerCase());
        if (idx == null) {
            return "";
        }
        final Object v = values[idx];
        if (v == null || v == ITable.NO_VALUE) {
            return "";
        }
        try {
            return DataType.asString(v);
        } catch (final TypeCastException e) {
            throw new AssertionError("column=" + name + ", value=" + v, e);
        }
    }

    private String pad(final String value, final FixedColumnDef def) {
        if (this.lengthType == LengthType.BYTE) {
            return this.padByte(value, def);
        }
        return this.padChar(value, def);
    }

    private String padChar(final String value, final FixedColumnDef def) {
        final int len = def.length();
        if (value.length() >= len) {
            return value.substring(0, len);
        }
        final String padding = def.pad().repeat(len - value.length());
        return def.leftAlign() ? value + padding : padding + value;
    }

    private String padByte(final String value, final FixedColumnDef def) {
        final int targetBytes = def.length();
        final String padChar = def.pad();
        final int padByteLen = padChar.getBytes(this.charset).length;

        String truncated = value;
        byte[] truncatedBytes = truncated.getBytes(this.charset);
        while (truncatedBytes.length > targetBytes) {
            final int cpCount = truncated.codePointCount(0, truncated.length());
            truncated = truncated.substring(0, truncated.offsetByCodePoints(0, cpCount - 1));
            truncatedBytes = truncated.getBytes(this.charset);
        }

        final int remaining = targetBytes - truncatedBytes.length;
        final int padCount = remaining / padByteLen;
        final String padding = padChar.repeat(padCount);
        final String spaceFill = " ".repeat(remaining - padCount * padByteLen);

        if (def.leftAlign()) {
            return truncated + padding + spaceFill;
        } else {
            return spaceFill + padding + truncated;
        }
    }

    public enum LengthType {
        CHAR, BYTE;

        public static LengthType of(final String value) {
            if ("byte".equalsIgnoreCase(value)) {
                return BYTE;
            }
            return CHAR;
        }
    }
}
