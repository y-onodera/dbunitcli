package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;

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
    public Parameter getParameter() {
        return this.parameter;
    }

    @Override
    public Stream<Source> getSourceStream() {
        return QueryDataSetProducer.super.getSourceStream();
    }

    @Override
    public void executeTable(final Source source) {
        try {
            final File file = new File(source.filePath());
            final String query = this.loadQuery(file);
            ComparableQueryDataSetProducer.LOGGER.info("produce - start fileName={},query={}", file.getName(), query);
            this.executeTable(this.connection.createQueryTable(this.getTableName(file), query), source);
            ComparableQueryDataSetProducer.LOGGER.info("produce - end   fileName={}", file.getName());
        } catch (final SQLException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected String loadQuery(final File aFile) {
        return this.getTemplateLoader()
                .render(aFile, this.getParameter())
                .replace(";", "");
    }

}
