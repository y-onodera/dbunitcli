package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;

public class ConvertOption extends CommandLineOption {

    @Option(name = "-src", usage = "export target", required = true)
    private File src;

    @Option(name = "-srcType", usage = "table | sql | csv | csvq | xls | xlsx ")
    private String srcType = "csv";

    private String[] args;

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(Parameter param) {
        super(param);
    }

    @Override
    public void parse(String[] args) throws Exception {
        this.args = args;
        super.parse(args);
    }

    public String getResultFileName() {
        String resultFile = "result";
        if (args[0].startsWith("@")) {
            resultFile = new File(args[0].replace("@", "")).getName();
            resultFile = resultFile.substring(0, resultFile.lastIndexOf("."));
        }
        return resultFile;
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.src)
                        .setSource(DataSourceType.fromString(this.srcType))
                        .build()
        );
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.srcType, this.src, "src");
    }

}
