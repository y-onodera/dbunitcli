package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.Map;

public class HeaderNameOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-headerName", usage = "comma separate header name. if set,all rows treat data rows")
    private String headerName;

    public HeaderNameOption(String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setHeaderName(this.headerName);
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-headerName", this.headerName);
        return result;
    }

}
