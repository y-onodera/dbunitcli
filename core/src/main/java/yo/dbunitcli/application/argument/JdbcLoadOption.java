package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class JdbcLoadOption extends JdbcOption implements ComparableDataSetParamOption{

    @Option(name = "-useJdbcMetaData", usage = "default false. whether load metaData from jdbc or not")
    private String useJdbcMetaData = "false";

    public JdbcLoadOption(String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData))
                .setDatabaseConnectionLoader(this.getDatabaseConnectionLoader());
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result= super.createOptionParam(args);
        result.put("-useJdbcMetaData",this.useJdbcMetaData);
        return result;
    }

}
