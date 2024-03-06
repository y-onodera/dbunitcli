package yo.dbunitcli.application.argument;

public abstract class DefaultArgumentsParser implements ArgumentsParser {

    private final String prefix;

    private ArgumentFilter argumentFilter = new DefaultArgumentFilter();

    private ArgumentMapper argumentMapper = new DefaultArgumentMapper();

    public DefaultArgumentsParser(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public ArgumentFilter getArgumentFilter() {
        return this.argumentFilter;
    }

    @Override
    public ArgumentMapper getArgumentMapper() {
        return this.argumentMapper;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    public void setArgumentFilter(final ArgumentFilter argumentFilter) {
        this.argumentFilter = argumentFilter;
    }

    public void setArgumentMapper(final ArgumentMapper argumentMapper) {
        this.argumentMapper = argumentMapper;
    }

}
