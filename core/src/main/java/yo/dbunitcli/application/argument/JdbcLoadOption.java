package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class JdbcLoadOption extends JdbcOption implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-useJdbcMetaData", description = "default false. whether load metaData from jdbc or not")
    private String useJdbcMetaData = "false";

    public JdbcLoadOption(final String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData))
                .setDatabaseConnectionLoader(this.getDatabaseConnectionLoader());
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = super.createOptionParam(args);
        result.put("-useJdbcMetaData", this.useJdbcMetaData);
        return result;
    }

}
