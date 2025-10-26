package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.util.stream.Stream;

public record ComparableNoneDataSetProducer(ComparableDataSetParam param) implements ComparableDataSetProducer {

    @Override
    public String getSrc() {
        return "";
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Stream.empty();
    }

    @Override
    public Runnable createExecuteTableTask(final Source source, final ComparableDataSetConsumer consumer) {
        return () -> {
        };
    }
}
