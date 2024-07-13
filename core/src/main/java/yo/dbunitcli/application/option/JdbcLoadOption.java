package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record JdbcLoadOption(
        String prefix
        , JdbcOption jdbc
        , boolean useJdbcMetaData
) implements ComparableDataSetParamOption {


    public JdbcLoadOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, new JdbcOption(prefix, dto.getJdbc())
                , Strings.isNotEmpty(dto.getUseJdbcMetaData())
                        && Boolean.parseBoolean(dto.getUseJdbcMetaData()));
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-useJdbcMetaData", this.useJdbcMetaData);
        result.addComponent("jdbc", this.jdbc.toCommandLineArgs());
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setUseJdbcMetaData(this.useJdbcMetaData)
                .setDatabaseConnectionLoader(this.jdbc.getDatabaseConnectionLoader());
    }

}
