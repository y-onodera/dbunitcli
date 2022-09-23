package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class ComparableQueryDataSetProducer extends ComparableDBDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableQueryDataSetProducer.class);
    private final Parameter parameter;

    public ComparableQueryDataSetProducer(ComparableDataSetParam param, Parameter parameter) throws DataSetException {
        super(param);
        this.parameter = parameter;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath()) && file.length() > 0) {
                try {
                    this.executeQuery(file);
                } catch (SQLException | IOException e) {
                    throw new DataSetException(e);
                }
            }
        }
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    public TemplateRender getTemplateLoader() {
        return this.getParam().getStTemplateLoader();
    }

    protected void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        String query = this.loadQuery(aFile);
        LOGGER.info("produce - start fileName={},query={}", aFile.getName(), query);
        this.executeTable(this.connection.createQueryTable(this.getTableName(aFile), query));
        LOGGER.info("produce - end   fileName={}", aFile.getName());
    }

    protected String loadQuery(File aFile) throws IOException {
        return this.getTemplateLoader()
                .render(aFile, this.getParameter())
                .replace(";", "");
    }

}
