package yo.dbunitcli.dataset.producer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingContext;

import java.io.File;
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

    @Override
    public Stream<Source> getSourceStream() {
        return QueryDataSetProducer.super.getSourceStream();
    }

    @Override
    public Runnable createTableMappingTask(final Source source, final ComparableTableMappingContext context) {
        return new QueryTableExecutor(source, context, this.param, this.connection, this.parameter);
    }

    private static class QueryTableExecutor extends ComparableDBDataSetProducer.DBTableExecutor {
        private final Parameter parameter;

        QueryTableExecutor(final Source source, final ComparableTableMappingContext context,
                           final ComparableDataSetParam param,
                           final IDatabaseConnection connection,
                           final Parameter parameter) {
            super(source, context, param, connection, null);
            this.parameter = parameter;
        }

        @Override
        protected ITable getTable(final String tableName) {
            try {
                final File file = new File(this.source.filePath());
                final String query = this.loadQuery(file);
                ComparableQueryDataSetProducer.LOGGER.info("produce - start fileName={},query={}", file.getName(), query);
                final ITable table = this.connection.createQueryTable(this.source.getTableName(), query);
                ComparableQueryDataSetProducer.LOGGER.info("produce - end   fileName={}", file.getName());
                return table;
            } catch (final SQLException | DataSetException e) {
                throw new AssertionError(e);
            }
        }

        protected String loadQuery(final File aFile) {
            return this.param.templateRender()
                    .render(aFile, this.parameter)
                    .replace(";", "");
        }
    }
}
