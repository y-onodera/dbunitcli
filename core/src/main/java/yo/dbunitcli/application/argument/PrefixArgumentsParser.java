package yo.dbunitcli.application.argument;

public abstract class PrefixArgumentsParser implements ArgumentsParser {

    private final String prefix;

    public PrefixArgumentsParser(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

}
