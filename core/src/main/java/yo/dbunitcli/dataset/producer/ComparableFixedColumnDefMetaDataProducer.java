package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;

public class ComparableFixedColumnDefMetaDataProducer extends ComparableDataSetProducerWrapper {

    private static final Column[] COLUMN_DEF_SCHEMA = {
            new Column("name", DataType.VARCHAR),
            new Column("length", DataType.INTEGER),
            new Column("align", DataType.VARCHAR),
            new Column("pad", DataType.VARCHAR)
    };

    /** -defaultLength未指定時の長さ */
    public static final String DEFAULT_LENGTH = "10";

    /** -align未指定時の寄せ */
    public static final String DEFAULT_ALIGN = "left";

    private final String[] lengths;
    private final int defaultLength;
    private final String align;

    public ComparableFixedColumnDefMetaDataProducer(final ComparableDataSetProducer delegate,
                                                      final String[] lengths, final int defaultLength, final String align) {
        super(delegate, COLUMN_DEF_SCHEMA);
        this.lengths = lengths;
        this.defaultLength = defaultLength;
        this.align = align;
    }

    /**
     * オプションの値（カンマ区切りの長さ・既定の長さ・寄せ）から作る。既定の長さ・寄せが空なら{@link #DEFAULT_LENGTH}・{@link #DEFAULT_ALIGN}
     */
    public static ComparableFixedColumnDefMetaDataProducer of(final ComparableDataSetProducer delegate
            , final String fixedLength, final String defaultLength, final String align) {
        final String[] lengths = fixedLength == null || fixedLength.isEmpty() ? new String[0] : fixedLength.split(",");
        return new ComparableFixedColumnDefMetaDataProducer(delegate, lengths, Integer.parseInt(
                (defaultLength == null || defaultLength.isEmpty() ? DEFAULT_LENGTH : defaultLength).trim())
                , align == null || align.isEmpty() ? DEFAULT_ALIGN : align);
    }

    public static Column[] outputSchema() {
        return COLUMN_DEF_SCHEMA.clone();
    }

    @Override
    protected void convertTable(final ITableMetaData metaData, final Conversion conversion) throws DataSetException {
        conversion.writeColumnRows(metaData.getTableName(), metaData.getColumns(), (column, index) -> {
            final int length = index < this.lengths.length ? Integer.parseInt(this.lengths[index].trim()) : this.defaultLength;
            return new Object[]{column.getColumnName(), length, this.align, " "};
        });
    }
}
