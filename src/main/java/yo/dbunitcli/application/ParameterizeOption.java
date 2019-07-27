package yo.dbunitcli.application;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ParameterizeOption extends CommandLineOption {

    @Option(name = "-param", usage = "directory or file extract data driven parameter at", required = true)
    private File param;

    @Option(name = "-paramType", usage = "table | sql | csv | csvq | xls | xlsx | : default csv")
    private String paramType = "csv";

    @Option(name = "-template", usage = "template file. data driven target argument", required = true)
    private File template;

    @Option(name = "-cmd", usage = "compare | exp | imp :data driven target cmd", required = true)
    private String cmd;

    private String templateArgs;

    public ParameterizeOption() {
        super(Maps.newHashMap());
    }

    public List<Map<String, Object>> loadParams() throws DataSetException {
        return this.getComparableDataSetLoader().loadParam(
                this.getDataSetParamBuilder()
                        .setSrc(this.param)
                        .setSource(DataSourceType.fromString(this.paramType))
                        .build()
        );
    }

    public String[] createArgs(Map<String, Object> aParam) {
        ST st = new ST(this.templateArgs, '$', '$');
        aParam.entrySet().forEach(it -> st.add(it.getKey(), it.getValue()));
        return st.render().split("\\r?\\n");
    }

    public Command createCommand() {
        switch (this.cmd) {
            case "compare":
                return new Compare();
            case "export":
                return new Export();
            case "import":
                return new Import();
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
        try {
            this.templateArgs = Files.asCharSource(this.template, Charset.forName(getEncoding())).read();
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
