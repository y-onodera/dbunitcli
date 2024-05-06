package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.JdbcOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
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

public class RunOption extends CommandLineOption<RunDto> {

    private final DataSetLoadOption src = new DataSetLoadOption("");
    private final TemplateRenderOption templateOption = new TemplateRenderOption("");
    private final JdbcOption jdbcOption = new JdbcOption("");
    private ScriptType scriptType = ScriptType.sql;

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
                .map(it -> new File(it.get(ComparableFileTableMetaData.PK.getColumnName()).toString()));
    }

    public Runner runner() {
        return this.getScriptType().createRunner(this);
    }

    @Override
    public RunDto toDto(final String[] args) {
        final RunDto dto = new RunDto();
        new CommandLineParser("", this.getArgumentMapper(), this.getArgumentFilter())
                .parseArgument(args, dto);
        new CommandLineParser(this.src.getPrefix()).parseArgument(args, dto.getDataSetLoad());
        new CommandLineParser(this.jdbcOption.getPrefix()).parseArgument(args, dto.getJdbc());
        new CommandLineParser(this.templateOption.getPrefix()).parseArgument(args, dto.getTemplateRender());
        return dto;
    }

    @Override
    public void setUpComponent(final RunDto dto) {
        super.setUpComponent(dto);
        if (dto.getScriptType() != null) {
            this.scriptType = dto.getScriptType();
        }
        this.templateOption.setUpComponent(dto.getTemplateRender());
        this.jdbcOption.setUpComponent(dto.getJdbc());
        this.src.setUpComponent(dto.getDataSetLoad());
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(args);
        result.putAll(this.src.createOptionParam(args));
        result.put("-scriptType", this.scriptType, ScriptType.class);
        result.putAll(this.templateOption.createOptionParam(args));
        if (result.get("-scriptType").equals(ScriptType.sql.name())) {
            result.putAll(this.jdbcOption.createOptionParam(args));
        }
        return result;
    }

    public enum ScriptType {
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
