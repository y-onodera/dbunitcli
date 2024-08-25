package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;

import java.io.File;

public record AntOption(
        String prefix
        , String baseDir
        , String target
) implements Option {

    public AntOption(final String baseDir, final String target) {
        this("", baseDir, target);
    }

    @Override
    public String baseDir() {
        return new File(Strings.isNotEmpty(this.baseDir) ? this.baseDir : ".")
                .getAbsoluteFile().toPath().normalize().toString();
    }

    @Override
    public String getPrefix() {
        return this.prefix();
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-antBaseDir", this.baseDir);
        result.put("-antTarget", this.target);
        return result;
    }
}
