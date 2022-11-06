package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.dataset.Parameter;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Aggregate {
    public static void main(final String[] args) throws Exception {
        final ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        final List<IDataSet> dataSets = Lists.newArrayList();
        final List<Map<String, Object>> params = options.loadParams();
        IntStream.range(0, params.size()).forEach(i -> {
            final Parameter param = new Parameter(i + 1, params.get(i));
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
