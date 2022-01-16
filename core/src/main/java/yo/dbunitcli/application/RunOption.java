package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.producer.ComparableFileTableMetaData;
import yo.dbunitcli.fileprocessor.AntRunner;
import yo.dbunitcli.fileprocessor.CmdRunner;
import yo.dbunitcli.fileprocessor.Runner;
import yo.dbunitcli.fileprocessor.SqlRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RunOption extends CommandLineOption {

    @Option(name = "-src", usage = "directory script exists or file should be run", required = true)
    private File src;

    @Option(name = "-scriptType", usage = "sql | bat | cmd")
    private String scriptType = "sql";

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

    public RunOption() {
        super(Parameter.none());
    }

    public RunOption(Parameter param) {
        super(param);
    }

    public File getSrc() {
        return this.src;
    }

    public ScriptType getScriptType() {
        return ScriptType.fromString(this.scriptType);
    }

    public List<File> targetFiles() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                        this.getDataSetParamBuilder()
                                .setSrc(this.src)
                                .setSource(DataSourceType.FILE)
                                .setExtension(this.scriptType)
                                .setMapIncludeMetaData(false)
                                .build())
                .toMap()
                .stream()
                .map(it -> new File(it.get(ComparableFileTableMetaData.PK.getColumnName()).toString()))
                .collect(Collectors.toList());
    }

    public Runner runner() {
        return this.getScriptType().createRunner(this);
    }

    @Override
    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.templateOption.parseArgument(expandArgs);
        if (!this.src.exists()) {
            throw new CmdLineException(parser, src + " is not exist", new IllegalArgumentException(src.toString()));
        }
    }

    enum ScriptType {
        CMD("cmd"), BAT("bat"), SQL("sql") {
            @Override
            public Runner createRunner(RunOption aOption) {
                return new SqlRunner(aOption.getWriteOption().getJdbcOption().getDatabaseConnectionLoader()
                        , aOption.getParameter().getMap()
                        , aOption.templateOption.getTemplateRender()
                );
            }
        }, Ant("xml") {
            @Override
            public Runner createRunner(RunOption aOption) {
                return new AntRunner(aOption.getParameter().getMap());
            }
        };

        private final String type;

        ScriptType(String type) {
            this.type = type;
        }

        static ScriptType fromString(String type) {
            return Arrays.stream(ScriptType.values())
                    .filter(it -> it.type.equals(type))
                    .findFirst()
                    .get();
        }

        public Runner createRunner(RunOption aOption) {
            return new CmdRunner(aOption.getParameter().getMap()
                    , aOption.templateOption.getTemplateRender()
            );
        }
    }
}
