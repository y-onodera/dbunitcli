package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class JdbcLoadOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final JdbcOption jdbc;
    private final String useJdbcMetaData;

    public JdbcLoadOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.jdbc = new JdbcOption(this.prefix, dto.getJdbc());
        if (Strings.isNotEmpty(dto.getUseJdbcMetaData())) {
            this.useJdbcMetaData = dto.getUseJdbcMetaData();
        } else {
            this.useJdbcMetaData = "false";
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData))
                .setDatabaseConnectionLoader(this.jdbc.getDatabaseConnectionLoader());
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = this.jdbc.toCommandLineArgs();
        result.put("-useJdbcMetaData", this.useJdbcMetaData);
        return result;
    }

}
