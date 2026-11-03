package yo.dbunitcli.dataset.producer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingTask;
import yo.dbunitcli.dataset.InlineSource;

import java.sql.SQLException;
import java.util.stream.Stream;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer implements QueryDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private final Parameter parameter;

    public ComparableQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        super(param);
        this.parameter = parameter;
    }

    @Override
    public Parameter parameter() {
        return this.parameter;
    }

    // 継承したtableの読み方ではなく、SQLとして読む
    @Override
    public Stream<? extends Source> getSrcFileSourceStream() {
        return QueryDataSetProducer.super.getSrcFileSourceStream();
    }

    @Override
    public Stream<Source> getInlineSourceStream(final InlineSource inlineSource) {
        return QueryDataSetProducer.super.getInlineSourceStream(inlineSource);
    }

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new QueryTableExecutor(source, this.param, this.connection, this.parameter);
    }

    private static class QueryTableExecutor extends ComparableDBDataSetProducer.DBTableExecutor {
        private final Parameter parameter;

        QueryTableExecutor(final Source source,
                           final ComparableDataSetParam param,
                           final IDatabaseConnection connection,
                           final Parameter parameter) {
            super(source, param, connection);
            this.parameter = parameter;
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new QueryTableExecutor(this.source, builder.build(), this.connection, this.parameter);
        }

        /**
         * テーブル名はクエリファイル名で決まる
         */
        @Override
        public Stream<TableMetaDataWithSource> outputTableCandidates() {
            return Stream.of(this.source.createMetaData(new Column[0]));
        }

        @Override
        protected DBTableExecutor withConnection(final IDatabaseConnection connection) {
            return new QueryTableExecutor(this.source, this.param, connection, this.parameter);
        }

        @Override
        protected ITable getTable(final String tableName) {
            try {
                final String query = QueryDataSetProducer.renderQuery(this.param, this.source, this.parameter).replace(";", "");
                ComparableQueryDataSetProducer.LOGGER.info("produce - start fileName={},query={}", this.source.fileName(), query);
                final ITable table = this.connection.createQueryTable(this.source.getTableName(), query);
                ComparableQueryDataSetProducer.LOGGER.info("produce - end   fileName={}", this.source.fileName());
                return table;
            } catch (final SQLException | DataSetException e) {
                throw new AssertionError(e);
            }
        }
    }
}
