package yo.dbunitcli.application;

import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.dataset.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Aggregate {
    public static void main(final String[] args) throws Exception {
        final ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        final List<IDataSet> dataSets = new ArrayList<>();
        options.loadParams().forEach(it -> {
            final Parameter param = new Parameter(dataSets.size() + 1, it);
            final ConvertOption exp = new ConvertOption(param);
            exp.parse(options.createArgs(param));
            dataSets.add(exp.targetDataSet());
        });
        final CompositeDataSet composite = new CompositeDataSet(dataSets.toArray(new IDataSet[0]));
        final IDataSetProducer producer = new DataSetProducerAdapter(composite);
        options.getConverterOption().setResultPath("result");
        producer.setConsumer(options.converter());
        producer.produce();
    }
}
