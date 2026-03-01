package yo.dbunitcli.application;

public class DefaultArgumentFunction implements ArgumentFunction {

    @Override
    public String[] apply(final String[] args, final String prefix) {
        return args;
    }
}
