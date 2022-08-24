package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.JdbcOption;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.producer.ComparableFileTableMetaData;
import yo.dbunitcli.fileprocessor.AntRunner;
import yo.dbunitcli.fileprocessor.CmdRunner;
import yo.dbunitcli.fileprocessor.Runner;
import yo.dbunitcli.fileprocessor.SqlRunner;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunOption extends CommandLineOption {

    @Option(name = "-src", usage = "directory script exists or file should be run", required = true)
    private File src;

    @Option(name = "-scriptType")
    private ScriptType scriptType = ScriptType.sql;

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

    private JdbcOption jdbcOption = new JdbcOption("");

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
        return this.scriptType;
    }

    public List<File> targetFiles() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                        this.getDataSetParamBuilder()
                                .setSrc(this.src)
                                .setSource(DataSourceType.file)
                                .setExtension(this.scriptType.getExtension())
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
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.jdbcOption.parseArgument(expandArgs);
        if (!this.src.exists()) {
            throw new CmdLineException(parser, src + " is not exist", new IllegalArgumentException(src.toString()));
        }
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-scriptType", this.scriptType, ScriptType.class);
        result.putFile("-src", this.src);
        result.putAll(this.templateOption.createOptionParam(args));
        if (result.get("-scriptType").equals(ScriptType.sql.name())) {
            result.putAll(this.jdbcOption.createOptionParam(args));
        }
        return result;
    }

    enum ScriptType {
        cmd, bat, sql {
            @Override
            public Runner createRunner(RunOption aOption) {
                return new SqlRunner(aOption.jdbcOption.getDatabaseConnectionLoader()
                        , aOption.getParameter().getMap()
                        , aOption.templateOption.getTemplateRender()
                );
            }
        }, ant {
            @Override
            public Runner createRunner(RunOption aOption) {
                return new AntRunner(aOption.getParameter().getMap());
            }

            @Override
            public String getExtension() {
                return "xml";
            }
        };

        public Runner createRunner(RunOption aOption) {
            return new CmdRunner(aOption.getParameter().getMap()
                    , aOption.templateOption.getTemplateRender()
            );
        }

        public String getExtension() {
            return this.toString();
        }
    }
}
