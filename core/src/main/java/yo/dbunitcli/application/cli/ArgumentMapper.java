package yo.dbunitcli.application.cli;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.CompositeDto;

public record ArgumentMapper(String prefix, ArgumentFunction function, ArgumentFilter filter) {

    public ArgumentMapper(final String prefix) {
        this(prefix, new DefaultArgumentFunction(), new DefaultArgumentFilter());
    }

    public ArgumentMapper(final String prefix, final ArgumentFunction argumentMapper) {
        this(prefix, argumentMapper, new DefaultArgumentFilter());
    }

    public void populate(final String[] args, final CompositeDto compositeDto) {
        compositeDto.dto().forEach(dto -> this.populate(args, dto));
    }

    public void populate(final String[] args, final Object dto) {
        final CommandLine cmdLine = new CommandLine(dto);
        final String[] targetArgs = this.function().apply(args, this.prefix());
        cmdLine.parseArgs(this.filter().filterArguments(this.prefix(), cmdLine, targetArgs));
    }
}
