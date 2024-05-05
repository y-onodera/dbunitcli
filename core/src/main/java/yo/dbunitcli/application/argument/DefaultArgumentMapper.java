package yo.dbunitcli.application.argument;

public class DefaultArgumentMapper implements ArgumentMapper {

    @Override
    public String[] map(final String[] args, final String prefix) {
        return args;
    }
}
