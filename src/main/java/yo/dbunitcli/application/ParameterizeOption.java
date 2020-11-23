package yo.dbunitcli.application;

import com.google.common.io.Files;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    @Option(name = "-param", usage = "directory or file extract data driven parameter at", required = true)
    private File param;

    @Option(name = "-paramType", usage = "table | sql | csv | csvq | xls | xlsx | fixed | reg | file | dir")
    private String paramType = "csv";

    @Option(name = "-includeMetaData", usage = "whether param include tableName and columns or not ")
    private String includeMetaData = "false";

    @Option(name = "-template", usage = "template file. data driven target argument", required = true)
    private File template;

    @Option(name = "-templateGroup", usage = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @Option(name = "-cmd", usage = "compare | convert :data driven target cmd", required = true)
    private String cmd;

    private STGroup stGroup;

    private String templateArgs;

    public ParameterizeOption() {
        super(Parameter.none());
    }

    public List<Map<String, Object>> loadParams() throws DataSetException {
        return this.getComparableDataSetLoader().loadParam(
                this.getDataSetParamBuilder()
                        .setSrc(this.param)
                        .setSource(DataSourceType.fromString(this.paramType))
                        .setMapIncludeMetaData(Boolean.parseBoolean(this.includeMetaData))
                        .build()
        );
    }

    public String[] createArgs(Parameter aParam) {
        ST st = new ST(this.stGroup, this.templateArgs);
        st.add("rowNumber", aParam.getRowNumber());
        aParam.getMap().forEach(st::add);
        return st.render().split("\\r?\\n");
    }

    public Command<?> createCommand() {
        switch (this.cmd) {
            case "compare":
                return new Compare();
            case "convert":
                return new Convert();
            case "generate":
                return new Generate();
            case "run":
                return new Run();
            default:
                throw new IllegalArgumentException("no executable command : " + this.cmd);
        }
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.paramType, this.param, "param");
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        this.stGroup = this.createSTGroup(this.templateGroup);
        try {
            this.templateArgs = Files.asCharSource(this.template, Charset.forName(getEncoding())).read();
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
