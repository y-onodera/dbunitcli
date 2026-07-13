package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.stream.Stream;

public abstract class ComparableDataSetProducerWrapper implements ComparableDataSetProducer, IDataSetConverter {

    protected final ComparableDataSetProducer delegate;

    private ComparableTableMappingContext outputContext;

    protected ComparableDataSetProducerWrapper(final ComparableDataSetProducer delegate) {
        this.delegate = delegate;
    }

    @Override
    public ComparableDataSetParam param() {
        return this.delegate.param();
    }

    @Override
    public Stream<? extends Source> getSourceStream() {
        return this.delegate.getSourceStream();
    }

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new DelegateCaptureTask(source, this.delegate.createTableMappingTask(source)
                .with(builder -> builder.setLoadData(false)));
    }

    @Override
    public boolean isExportEmptyTable() {
        return true;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        try {
            this.startTable(tableMetaData);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void row(final Object[] values) {
        // 加工結果はstartTable()で確定済みのため行データは使用しない
    }

    @Override
    public void endTable() {
        // 加工結果はstartTable()で確定済み
    }

    @Override
    public IDataSetConverter split() {
        return this;
    }

    protected ComparableTableMapper createOutputMapper(final TableMetaDataWithSource metaData) {
        return this.outputContext.createMapper(metaData);
    }

    /**
     * サブクラス共通の「列メタデータを1列1行に変換する」処理。tableNameのSource.NONEラップ・
     * DefaultTableMetaData生成・startTable/addRow/endTableの反復を一括で行う。
     */
    protected void writeColumnRows(final String tableName, final Column[] outputSchema,
                                   final Column[] sourceColumns, final RowBuilder rowBuilder) {
        final ComparableTableMapper mapper = this.createOutputMapper(
                Source.NONE.tableName(tableName).wrap(new DefaultTableMetaData(tableName, outputSchema)));
        mapper.startTable();
        for (int i = 0; i < sourceColumns.length; i++) {
            mapper.addRow(rowBuilder.buildRow(sourceColumns[i], i));
        }
        mapper.endTable();
    }

    @FunctionalInterface
    protected interface RowBuilder {
        Object[] buildRow(Column column, int index);
    }

    private final class DelegateCaptureTask implements ComparableTableMappingTask {

        private final Source source;
        private final ComparableTableMappingTask delegateTask;

        DelegateCaptureTask(final Source source, final ComparableTableMappingTask delegateTask) {
            this.source = source;
            this.delegateTask = delegateTask;
        }

        @Override
        public Source source() {
            return this.source;
        }

        @Override
        public ComparableDataSetParam param() {
            return this.delegateTask.param();
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            ComparableDataSetProducerWrapper.this.outputContext = context;
            final ComparableTableMappingContext captureContext = new ComparableTableMappingContext(
                    this.delegateTask.param().tableSeparators(), ComparableDataSetProducerWrapper.this);
            captureContext.open();
            this.delegateTask.run(captureContext);
            captureContext.close();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new DelegateCaptureTask(this.source, this.delegateTask.with(builder));
        }
    }
}
