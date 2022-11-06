package yo.dbunitcli.application;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
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
import java.util.Map;
import java.util.stream.Stream;

public class RunOption extends CommandLineOption {

    @Option(name = "-scriptType")
    private ScriptType scriptType = ScriptType.sql;

    private final DataSetLoadOption src = new DataSetLoadOption("");

    private final TemplateRenderOption templateOption = new TemplateRenderOption("");

    private final JdbcOption jdbcOption = new JdbcOption("");

    public RunOption() {
        super(Parameter.none());
    }

    public RunOption(final Parameter param) {
        super(param);
    }

    public ScriptType getScriptType() {
        return this.scriptType;
    }

    public Stream<File> targetFiles() {
        return this.getComparableDataSetLoader().loadDataSet(
                        this.src.getParam()
                                .setSource(DataSourceType.file)
                                .setExtension(this.scriptType.getExtension())
                                .setMapIncludeMetaData(false)
                                .setLoadData(true)
                                .build())
                .toMap()
                .stream()
                .map(it -> new File(it.get(ComparableFileTableMetaData.PK.getColumnName()).toString()));
    }

    public Runner runner() {
        return this.getScriptType().createRunner(this);
    }

    @Override
    public void setUpComponent(final CmdLineParser parser, final String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.jdbcOption.parseArgument(expandArgs);
        this.src.parseArgument(expandArgs);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.src.createOptionParam(args));
        result.put("-scriptType", this.scriptType, ScriptType.class);
        result.putAll(this.templateOption.createOptionParam(args));
        if (result.get("-scriptType").equals(ScriptType.sql.name())) {
            result.putAll(this.jdbcOption.createOptionParam(args));
        }
        return result;
    }

    enum ScriptType {
        cmd, bat, sql {
            @Override
            public Runner createRunner(final RunOption aOption) {
                return new SqlRunner(aOption.jdbcOption.getDatabaseConnectionLoader()
                        , aOption.getParameter().getMap()
                        , aOption.templateOption.getTemplateRender()
                );
            }
        }, ant {
            @Override
            public Runner createRunner(final RunOption aOption) {
                return new AntRunner(aOption.getParameter().getMap());
            }

            @Override
            public String getExtension() {
                return "xml";
            }
        };

        public Runner createRunner(final RunOption aOption) {
            return new CmdRunner(aOption.getParameter().getMap()
                    , aOption.templateOption.getTemplateRender()
            );
        }

        public String getExtension() {
            return this.toString();
        }
    }
}
