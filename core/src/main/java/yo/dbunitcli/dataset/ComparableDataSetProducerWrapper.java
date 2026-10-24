package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class ComparableDataSetProducerWrapper implements ComparableDataSetProducer, IDataSetConverter {

    protected final ComparableDataSetProducer delegate;

    /**
     * ソースごとの変換結果。unit=tableはテーブル一覧の作成とテーブルごとの出力で同じタスクを2回実行するため、
     * 2回目以降は保持した結果を出力する（DB入力ではメタデータ照会を繰り返さない）
     */
    private final Map<Source, List<ConvertedTable>> converted = new HashMap<>();

    /**
     * 変換中のソースの結果を受け取る（startTable→writeColumnRowsで追加する）
     */
    private List<ConvertedTable> converting;

    protected ComparableDataSetProducerWrapper(final ComparableDataSetProducer delegate) {
        this.delegate = delegate;
    }

    @Override
    public ComparableDataSetParam param() {
        return this.delegate.param();
    }

    @Override
    public void close() {
        this.delegate.close();
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

    /**
     * サブクラス共通の「列メタデータを1列1行に変換する」処理。tableNameのSource.NONEラップ・
     * DefaultTableMetaData生成・startTable/addRow/endTableの反復を一括で行う。
     */
    protected void writeColumnRows(final String tableName, final Column[] outputSchema,
                                   final Column[] sourceColumns, final RowBuilder rowBuilder) {
        final List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < sourceColumns.length; i++) {
            rows.add(rowBuilder.buildRow(sourceColumns[i], i));
        }
        this.converting.add(new ConvertedTable(
                Source.NONE.tableName(tableName).wrap(new DefaultTableMetaData(tableName, outputSchema)), rows));
    }

    private record ConvertedTable(TableMetaDataWithSource metaData, List<Object[]> rows) {

        void writeTo(final ComparableTableMappingContext context) {
            final ComparableTableMapper mapper = context.createMapper(this.metaData);
            mapper.startTable();
            this.rows.forEach(mapper::addRow);
            mapper.endTable();
        }
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
            ComparableDataSetProducerWrapper.this.converted.computeIfAbsent(this.source, k -> this.convert())
                    .forEach(it -> it.writeTo(context));
        }

        /**
         * delegateのタスクを実行し、読み込んだテーブルのメタデータを変換する（行データは読み込みの有無によらず使わない）
         */
        private List<ConvertedTable> convert() {
            final List<ConvertedTable> result = new ArrayList<>();
            ComparableDataSetProducerWrapper.this.converting = result;
            try {
                final ComparableTableMappingContext captureContext = new ComparableTableMappingContext(
                        this.delegateTask.param().tableSeparators(), ComparableDataSetProducerWrapper.this);
                captureContext.open();
                this.delegateTask.run(captureContext);
                captureContext.close();
            } finally {
                ComparableDataSetProducerWrapper.this.converting = null;
            }
            return result;
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new DelegateCaptureTask(this.source, this.delegateTask.with(builder));
        }
    }
}
