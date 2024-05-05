package yo.dbunitcli.application.cli;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.CompositeDto;

public record CommandLineParser(String prefix, ArgumentMapper argumentMapper, ArgumentFilter argumentFilter) {
    public CommandLineParser(final String prefix) {
        this(prefix, new DefaultArgumentMapper(), new DefaultArgumentFilter());
    }

    public CommandLineParser(final String prefix, final ArgumentMapper argumentMapper) {
        this(prefix, argumentMapper, new DefaultArgumentFilter());
    }

    public void parseArgument(final String[] args, final CompositeDto compositeDto) {
        compositeDto.dto().forEach(dto -> this.parseArgument(args, dto));
    }

    public void parseArgument(final String[] args, final Object dto) {
        final CommandLine cmdLine = new CommandLine(dto);
        final String[] targetArgs = this.argumentMapper().map(args, this.prefix());
        cmdLine.parseArgs(this.argumentFilter().filterArguments(this.prefix(), cmdLine, targetArgs));
    }
}
