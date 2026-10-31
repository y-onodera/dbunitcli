package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.resource.Workers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 包んだproducerが読み込んだテーブルのメタデータを、列ごとに1行の記述子テーブル（列はcolumnDefSchema）に変換する
 */
public abstract class ComparableDataSetProducerWrapper implements ComparableDataSetProducer {

    protected final ComparableDataSetProducer delegate;

    private final Column[] columnDefSchema;

    /**
     * ソースごとの変換結果。1つのソースから複数のテーブルを作る場合、unit=tableはテーブルごとにソースのタスクを実行するため、
     * 2回目以降は保持した結果を出力する（DB入力ではメタデータ照会を繰り返さない）。ソースが異なれば別スレッドから同時に使われる
     */
    private final Map<Source, List<ConvertedTable>> converted = new ConcurrentHashMap<>();

    protected ComparableDataSetProducerWrapper(final ComparableDataSetProducer delegate, final Column[] columnDefSchema) {
        this.delegate = delegate;
        this.columnDefSchema = columnDefSchema;
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
                .with(builder -> builder.setLoadData(false)), true, null);
    }

    /**
     * 読み込んだテーブル1つを変換し、conversion.writeColumnRowsで出力する
     */
    protected abstract void convertTable(ITableMetaData metaData, Conversion conversion) throws DataSetException;

    private TableMetaDataWithSource outputMetaData(final String tableName) {
        return Source.NONE.tableName(tableName).wrap(new DefaultTableMetaData(tableName, this.columnDefSchema));
    }

    @FunctionalInterface
    protected interface RowBuilder {
        Object[] buildRow(Column column, int index);
    }

    private record ConvertedTable(TableMetaDataWithSource metaData, List<Object[]> rows) {

        void writeTo(final ComparableTableMappingContext context) {
            final ComparableTableMapper mapper = context.createMapper(this.metaData);
            mapper.startTable();
            this.rows.forEach(mapper::addRow);
            mapper.endTable();
        }
    }

    private static final class TableNameCollector extends MetaDataCapture {

        private final Set<String> tableNames = new LinkedHashSet<>();

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.tableNames.add(metaData.getTableName());
        }
    }

    /**
     * 1ソース分の変換。変換結果と、変換を実行する作業スレッドの資源を持つ
     */
    protected final class Conversion extends MetaDataCapture {

        private final List<ConvertedTable> tables = new ArrayList<>();

        private final Workers.WorkerResources resources;

        private Conversion(final Workers.WorkerResources resources) {
            this.resources = resources;
        }

        /**
         * @return 作業スレッド用に差し替えたタスクの変換なら、その作業スレッドの資源（DB接続など）。元のタスク（最初の作業スレッド・逐次実行）ならnull
         */
        public Workers.WorkerResources workerResources() {
            return this.resources;
        }

        /**
         * サブクラス共通の「列メタデータを1列1行に変換する」処理。sourceColumnsの各列をrowBuilderで1行にする
         */
        public void writeColumnRows(final String tableName, final Column[] sourceColumns, final RowBuilder rowBuilder) {
            final List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < sourceColumns.length; i++) {
                rows.add(rowBuilder.buildRow(sourceColumns[i], i));
            }
            this.tables.add(new ConvertedTable(ComparableDataSetProducerWrapper.this.outputMetaData(tableName), rows));
        }

        @Override
        public void startTable(final ITableMetaData metaData) throws DataSetException {
            ComparableDataSetProducerWrapper.this.convertTable(metaData, this);
        }
    }

    /**
     * delegateのタスクを実行し、読み込んだテーブルのメタデータを1列1行に変換して出力する。
     * 行を読まないタスク（テーブル一覧の作成）では変換せず、テーブル名とcolumnDefSchemaだけを出力する
     */
    private final class DelegateCaptureTask implements ComparableTableMappingTask {

        private final Source source;
        private final ComparableTableMappingTask delegateTask;
        /**
         * delegateは行を使わないため常に読まない。このタスクが行（変換結果）を出力するかはparam.loadData()で表す
         */
        private final ComparableDataSetParam param;
        private final Workers.WorkerResources resources;

        DelegateCaptureTask(final Source source, final ComparableTableMappingTask delegateTask, final boolean loadRows
                , final Workers.WorkerResources resources) {
            this.source = source;
            this.delegateTask = delegateTask;
            this.param = delegateTask.param().toBuilder().setLoadData(loadRows).build();
            this.resources = resources;
        }

        @Override
        public Source source() {
            return this.source;
        }

        @Override
        public ComparableDataSetParam param() {
            return this.param;
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            if (!this.param.loadData()) {
                this.tableNames().forEach(tableName -> {
                    final ComparableTableMapper mapper = context.createMapper(
                            ComparableDataSetProducerWrapper.this.outputMetaData(tableName));
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
        public ComparableTableMappingTask forWorker(final Workers.WorkerResources resources) {
            return new DelegateCaptureTask(this.source, this.delegateTask.forWorker(resources), this.param.loadData(), resources);
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            final boolean loadRows = builder.isLoadData();
            return new DelegateCaptureTask(this.source, this.delegateTask.with(builder.setLoadData(false)), loadRows, this.resources);
        }

        private List<ConvertedTable> convert() {
            final Conversion conversion = new Conversion(this.resources);
            this.runDelegate(conversion);
            return conversion.tables;
        }

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
}
