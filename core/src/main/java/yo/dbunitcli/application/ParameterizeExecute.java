package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ParameterizeExecute {

    public static void main(final String[] args) throws Exception {
        final ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        final List<Map<String, Object>> params = options.loadParams();
        IntStream.range(0, params.size()).forEach(i -> {
            final Map<String, Object> param = params.get(i);
            final Parameter parameter = new Parameter(i + 1, param);
            options.createCommand(param).exec(options.createArgs(parameter), parameter);
        });
    }

}
