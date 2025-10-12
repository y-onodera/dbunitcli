package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.TableMetaDataWithSource;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private final Parameter parameter;

    public ComparableQueryDataSetProducer(final ComparableDataSetParam param, final Parameter parameter) {
        super(param);
        this.parameter = parameter;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableQueryDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.getParam().tableNameFilter().predicate(this.getTableName(it)))
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        ComparableQueryDataSetProducer.LOGGER.info("produce() - end");
    }

    public Parameter getParameter() {
        return this.parameter;
    }

    public TemplateRender getTemplateLoader() {
        return this.getParam().templateRender();
    }

    protected void produceFromFile(final File aFile) {
        try {
            final String query = this.loadQuery(aFile);
            ComparableQueryDataSetProducer.LOGGER.info("produce - start fileName={},query={}", aFile.getName(), query);
            this.executeTable(this.connection.createQueryTable(this.getTableName(aFile), query)
                    , TableMetaDataWithSource.fileInfo(aFile, this.addFileInfo));
            ComparableQueryDataSetProducer.LOGGER.info("produce - end   fileName={}", aFile.getName());
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
