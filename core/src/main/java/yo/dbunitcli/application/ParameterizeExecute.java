package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

public class ParameterizeExecute {

    public static void main(final String[] args) throws Exception {
        final ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (final Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        final Integer[] rowNum = new Integer[]{0};
        options.loadParams().forEach(it -> {
            final Parameter parameter = new Parameter(rowNum[0]++, it);
            options.createCommand(it).exec(options.createArgs(parameter), parameter);
        });
    }

}
