package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record JdbcLoadOption(
        String prefix
        , JdbcOption jdbc
        , String useJdbcMetaData
) implements ComparableDataSetParamOption {

    public JdbcLoadOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, new JdbcOption(prefix, dto.getJdbc())
                , Strings.isNotEmpty(dto.getUseJdbcMetaData()) ? dto.getUseJdbcMetaData() : "false");
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .putAll(this.jdbc.toParameters())
                .putFlag("-useJdbcMetaData", this.useJdbcMetaData);
    }

    @Override
    public List<Violation> validate() {
        return Option.concat(this.jdbc.validate(), this.validateBoolean("-useJdbcMetaData", this.useJdbcMetaData));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setDatabaseConnectionLoader(this.jdbc.getDatabaseConnectionLoader())
                .setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData));
    }

}
