package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.util.stream.Stream;

public class ComparableNoneDataSetProducer implements ComparableDataSetProducer {
    private final ComparableDataSetParam param;
    private ComparableDataSetConsumer consumer;

    public ComparableNoneDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer consumer) throws DataSetException {
        this.consumer = consumer;
    }

    @Override
    public ComparableDataSetConsumer getConsumer() {
        return this.consumer;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public String getSrc() {
        return "";
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Stream.empty();
    }

    @Override
    public void executeTable(final Source source) {

    }
}
