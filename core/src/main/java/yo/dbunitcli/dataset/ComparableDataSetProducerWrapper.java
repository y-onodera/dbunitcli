package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class ComparableDataSetProducerWrapper implements ComparableDataSetProducer, IDataSetConverter {

    protected final ComparableDataSetProducer delegate;

    /**
     * ソースごとの変換結果。1つのソースから複数のテーブルを作る場合、unit=tableはテーブルごとにソースのタスクを実行するため、
     * 2回目以降は保持した結果を出力する（DB入力ではメタデータ照会を繰り返さない）。ソースが異なれば別スレッドから同時に使われる
     */
    private final Map<Source, List<ConvertedTable>> converted = new ConcurrentHashMap<>();

    /**
     * 変換中のソースの結果を受け取る（startTable→writeColumnRowsで追加する）。変換はスレッドごとに独立して行う
     */
    private final ThreadLocal<List<ConvertedTable>> converting = new ThreadLocal<>();

    /**
     * 変換中のタスクが作業スレッド用に差し替えられていれば、その作業スレッドの資源（DB接続など）
     */
    private final ThreadLocal<ParallelTableMapping.WorkerResources> workerResources = new ThreadLocal<>();

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

    /**
     * 変換はソースごとに独立しているため、包んだproducerが並列読込に対応すれば並列化できる
     */
    @Override
    public boolean isParallelizableSource() {
        return this.delegate.isParallelizableSource();
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
     * writeColumnRowsで出力するテーブルの列。テーブル一覧の作成（行を読まない）では、変換せずにこの列だけを返す
     */
    protected abstract Column[] columnDefSchema();

    /**
     * 変換中のタスクが使う作業スレッドの資源。元のタスク（最初の作業スレッド・逐次実行）ならnull
     */
    protected ParallelTableMapping.WorkerResources workerResources() {
        return this.workerResources.get();
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
        this.converting.get().add(new ConvertedTable(ComparableDataSetProducerWrapper.outputMetaData(tableName, outputSchema), rows));
    }

    private static TableMetaDataWithSource outputMetaData(final String tableName, final Column[] outputSchema) {
        return Source.NONE.tableName(tableName).wrap(new DefaultTableMetaData(tableName, outputSchema));
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

    /**
     * delegateのタスクを実行し、読み込んだテーブルのメタデータを1列1行に変換して出力する。
     * テーブル一覧の作成（loadRows=false）では変換せず、テーブル名とcolumnDefSchema()だけを出力する
     */
    private final class DelegateCaptureTask implements ComparableTableMappingTask {

        private final Source source;
        private final ComparableTableMappingTask delegateTask;
        private final boolean loadRows;
        private final ParallelTableMapping.WorkerResources resources;

        DelegateCaptureTask(final Source source, final ComparableTableMappingTask delegateTask) {
            this(source, delegateTask, true, null);
        }

        private DelegateCaptureTask(final Source source, final ComparableTableMappingTask delegateTask, final boolean loadRows
                , final ParallelTableMapping.WorkerResources resources) {
            this.source = source;
            this.delegateTask = delegateTask;
            this.loadRows = loadRows;
            this.resources = resources;
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
            if (!this.loadRows) {
                this.tableNames().forEach(tableName -> {
                    final ComparableTableMapper mapper = context.createMapper(
                            ComparableDataSetProducerWrapper.outputMetaData(tableName, ComparableDataSetProducerWrapper.this.columnDefSchema()));
                    mapper.startTable();
                    mapper.endTable();
                });
                return;
            }
            List<ConvertedTable> result = ComparableDataSetProducerWrapper.this.converted.get(this.source);
            if (result == null) {
                result = this.convert();
                ComparableDataSetProducerWrapper.this.converted.put(this.source, result);
            }
            result.forEach(it -> it.writeTo(context));
        }

        /**
         * 出力テーブル名は変換元のテーブル名と同じ
         */
        @Override
        public Stream<TableMetaDataWithSource> outputTableCandidates() {
            return this.delegateTask.outputTableCandidates();
        }

        @Override
        public ComparableTableMappingTask forWorker(final ParallelTableMapping.WorkerResources resources) {
            return new DelegateCaptureTask(this.source, this.delegateTask.forWorker(resources), this.loadRows, resources);
        }

        /**
         * テーブル一覧の作成ではbuilderで行を読まない指定になる。delegateは行を使わないため常に読まない
         */
        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            final boolean rows = builder.isLoadData();
            return new DelegateCaptureTask(this.source, this.delegateTask.with(builder.setLoadData(false)), rows, this.resources);
        }

        /**
         * delegateのタスクを実行し、読み込んだテーブルのメタデータを変換する（行データは使わない）
         */
        private List<ConvertedTable> convert() {
            final List<ConvertedTable> result = new ArrayList<>();
            ComparableDataSetProducerWrapper.this.converting.set(result);
            ComparableDataSetProducerWrapper.this.workerResources.set(this.resources);
            try {
                this.runDelegate(ComparableDataSetProducerWrapper.this);
            } finally {
                ComparableDataSetProducerWrapper.this.converting.remove();
                ComparableDataSetProducerWrapper.this.workerResources.remove();
            }
            return result;
        }

        /**
         * delegateのタスクを実行し、読み込んだテーブルの名前だけを集める
         */
        private Set<String> tableNames() {
            final TableNameCollector collector = new TableNameCollector();
            this.runDelegate(collector);
            return collector.tableNames;
        }

        private void runDelegate(final IDataSetConverter converter) {
            final ComparableTableMappingContext captureContext = new ComparableTableMappingContext(
                    this.delegateTask.param().tableSeparators(), converter);
            captureContext.open();
            this.delegateTask.run(captureContext);
            captureContext.close();
        }
    }

    private static final class TableNameCollector implements IDataSetConverter {

        private final Set<String> tableNames = new LinkedHashSet<>();

        @Override
        public boolean isExportEmptyTable() {
            return true;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.tableNames.add(metaData.getTableName());
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            this.startTable(tableMetaData);
        }

        @Override
        public void row(final Object[] values) {
            // テーブル名だけを集める
        }

        @Override
        public void endTable() {
            // テーブル名だけを集める
        }

        @Override
        public IDataSetConverter split() {
            return this;
        }
    }
}
