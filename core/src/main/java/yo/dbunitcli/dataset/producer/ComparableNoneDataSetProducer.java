package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

public class ComparableNoneDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableNoneDataSetProducer.class);
    private final ComparableDataSetParam param;
    private ComparableDataSet consumer;

    public ComparableNoneDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
    }

    @Override
    public void setConsumer(final ComparableDataSet consumer) throws DataSetException {
        this.consumer = consumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableNoneDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        this.consumer.endDataSet();
        ComparableNoneDataSetProducer.LOGGER.info("produce() - end");
    }

    @Override
    public String getSrc() {
        return "";
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }
}
