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
import java.util.stream.Stream;

public class RunOption extends CommandLineOption<RunDto> {

    private final DataSetLoadOption srcData;
    private final TemplateRenderOption templateOption;
    private final JdbcOption jdbcOption;
    private final ScriptType scriptType;

    public static RunDto toDto(final String[] args) {
        final RunDto dto = new RunDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("src").parseArgument(args, dto.getSrcData());
        new CommandLineParser("jdbc").parseArgument(args, dto.getJdbcOption());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateOption());
        return dto;
    }

    public RunOption(final String resultFile, final RunDto dto, final Parameter param) {
        super(resultFile, dto, param);
        if (dto.getScriptType() != null) {
            this.scriptType = dto.getScriptType();
        } else {
            this.scriptType = ScriptType.sql;
        }
        this.templateOption = new TemplateRenderOption("template", dto.getTemplateOption());
        this.jdbcOption = new JdbcOption("jdbc", dto.getJdbcOption());
        this.srcData = new DataSetLoadOption("src", dto.getSrcData());
    }

    public ScriptType getScriptType() {
        return this.scriptType;
    }

    public Stream<File> targetFiles() {
        return this.getComparableDataSetLoader().loadDataSet(
                        this.srcData.getParam()
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
    public RunDto toDto() {
        return RunOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.addComponent("srcData", this.srcData.toCommandLineArgs()
                .remove("-srcType")
                .remove("-extension")
                .remove("-includeMetaData")
                .remove("-loadData")
        );
        result.put("-scriptType", this.scriptType, ScriptType.class);
        result.addComponent("templateOption", this.templateOption.toCommandLineArgs());
        if (result.get("-scriptType").equals(ScriptType.sql.name())) {
            result.addComponent("jdbcOption", this.jdbcOption.toCommandLineArgs());
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
