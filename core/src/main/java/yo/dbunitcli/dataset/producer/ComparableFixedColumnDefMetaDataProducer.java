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

    private final String[] lengths;
    private final int defaultLength;
    private final String align;

    public ComparableFixedColumnDefMetaDataProducer(final ComparableDataSetProducer delegate,
                                                      final String[] lengths, final int defaultLength, final String align) {
        super(delegate);
        this.lengths = lengths;
        this.defaultLength = defaultLength;
        this.align = align;
    }

    public static Column[] outputSchema() {
        return COLUMN_DEF_SCHEMA.clone();
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        this.writeColumnRows(metaData.getTableName(), COLUMN_DEF_SCHEMA, metaData.getColumns(), (column, index) -> {
            final int length = index < this.lengths.length ? Integer.parseInt(this.lengths[index].trim()) : this.defaultLength;
            return new Object[]{column.getColumnName(), length, this.align, " "};
        });
    }
}
