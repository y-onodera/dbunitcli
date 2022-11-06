package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer {

    private static final Logger LOGGER = LogManager.getLogger();
    private final Parameter parameter;

    public ComparableQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        super(param);
        this.parameter = parameter;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(file -> this.filter.predicate(file.getAbsolutePath()) && file.length() > 0)
                .forEach(this::execute);
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    public TemplateRender getTemplateLoader() {
        return this.getParam().getStTemplateLoader();
    }

    protected void execute(final File aFile) {
        try {
            final String query = this.loadQuery(aFile);
            LOGGER.info("produce - start fileName={},query={}", aFile.getName(), query);
            this.executeTable(this.connection.createQueryTable(this.getTableName(aFile), query));
            LOGGER.info("produce - end   fileName={}", aFile.getName());
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
