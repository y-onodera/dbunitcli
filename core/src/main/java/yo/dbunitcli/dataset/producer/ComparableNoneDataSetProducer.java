package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;

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
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new ComparableTableMappingTask() {
            @Override
            public void run(final ComparableTableMappingContext context) {
            }

            @Override
            public Source source() {
                return source;
            }
        };
    }
}
