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
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-antTarget", this.target);
    }
}
