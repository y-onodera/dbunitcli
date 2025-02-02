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
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .putAll(this.jdbc.toCommandLineArgs())
                .put("-useJdbcMetaData", this.useJdbcMetaData);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDatabaseConnectionLoader(this.jdbc.getDatabaseConnectionLoader())
                .setUseJdbcMetaData(this.useJdbcMetaData);
    }

}
