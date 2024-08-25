package yo.dbunitcli.application.option;

public record AntOption(
        String prefix
        , String target
) implements Option {

    public AntOption(final String target) {
        this("", target);
    }

    @Override
    public String getPrefix() {
        return this.prefix();
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-antTarget", this.target);
        return result;
    }
}
