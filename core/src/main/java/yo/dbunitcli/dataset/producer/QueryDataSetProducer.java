package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.util.stream.Stream;

public interface QueryDataSetProducer extends ComparableDataSetProducer {

    Parameter parameter();

    @Override
    default Stream<? extends Source> getSourceStream() {
        return ComparableDataSetProducer.super.getSourceStream();
    }
}
