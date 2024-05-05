package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class JdbcLoadOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final JdbcOption jdbc;
    private String useJdbcMetaData = "false";

    public JdbcLoadOption(final String prefix) {
        this.prefix = prefix;
        this.jdbc = new JdbcOption(this.prefix);
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
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = this.jdbc.createOptionParam(args);
        result.put("-useJdbcMetaData", this.useJdbcMetaData);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        if (Strings.isNotEmpty(dto.getUseJdbcMetaData())) {
            this.useJdbcMetaData = dto.getUseJdbcMetaData();
        }
        this.jdbc.setUpComponent(dto.getJdbc());
    }

}
