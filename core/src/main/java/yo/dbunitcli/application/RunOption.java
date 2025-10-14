package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.AntOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.JdbcOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.producer.ComparableFileTableMetaData;
import yo.dbunitcli.fileprocessor.AntRunner;
import yo.dbunitcli.fileprocessor.CmdRunner;
import yo.dbunitcli.fileprocessor.Runner;
import yo.dbunitcli.fileprocessor.SqlRunner;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.util.stream.Stream;

public record RunOption(
        Parameter parameter
        , ScriptType scriptType
        , DataSetLoadOption srcData
        , TemplateRenderOption templateOption
        , JdbcOption jdbcOption
        , AntOption antOption
        , String baseDir
) implements CommandLineOption<RunDto> {

    public static RunDto toDto(final String[] args) {
        final RunDto dto = new RunDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        dto.getSrcData().setSrcType(DataSourceType.file);
        dto.getSrcData().setIncludeMetaData("false");
        dto.getSrcData().setLoadData("true");
        new CommandLineParser("src").parseArgument(args, dto.getSrcData());
        new CommandLineParser("jdbc").parseArgument(args, dto.getJdbcOption());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateOption());
        return dto;
    }

    public RunOption(final RunDto dto, final Parameter param) {
        this(param
                , dto.getScriptType() != null ? dto.getScriptType() : ScriptType.sql
                , new DataSetLoadOption("src", dto.getSrcData())
                , new TemplateRenderOption("template", dto.getTemplateOption())
                , new JdbcOption("jdbc", dto.getJdbcOption())
                , new AntOption(dto.getAntTarget())
                , dto.getBaseDir()
        );
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
                .map(it -> FileResources.searchDatasetBase(it.get(ComparableFileTableMetaData.PK.getColumnName()).toString()));
    }

    public Runner runner() {
        return this.scriptType().createRunner(this);
    }

    @Override
    public RunDto toDto() {
        return RunOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        final CommandLineArgsBuilder result = new CommandLineArgsBuilder()
                .addComponent("srcData", this.srcData.toCommandLineArgsBuilder()
                        .remove("-src.srcType")
                        .remove("-src.extension")
                        .remove("-src.includeMetaData")
                        .remove("-src.loadData")
                        .build()
                )
                .put("-scriptType", this.scriptType, ScriptType.class);
        if (this.scriptType == ScriptType.sql) {
            result.addComponent("templateOption", this.templateOption.toCommandLineArgs())
                    .addComponent("jdbcOption", this.jdbcOption.toCommandLineArgs());
        } else if (this.scriptType == ScriptType.ant) {
            result.putAll(this.antOption.toCommandLineArgs());
        }
        if (this.scriptType != ScriptType.sql) {
            result.put("-baseDir", this.baseDir);
        }
        return result;
    }

    public File getBaseDir() {
        return Strings.isNotEmpty(this.baseDir())
                ? FileResources.searchDatasetBase(this.baseDir())
                : FileResources.datasetDir();
    }

    public enum ScriptType {
        cmd, bat, sql {
            @Override
            public Runner createRunner(final RunOption aOption) {
                return new SqlRunner(aOption.jdbcOption.getDatabaseConnectionLoader()
                        , aOption.parameter()
                        , aOption.templateOption.getTemplateRender()
                );
            }
        }, ant {
            @Override
            public Runner createRunner(final RunOption aOption) {
                return new AntRunner(aOption.getBaseDir(), aOption.antOption().target(), aOption.parameter());
            }

            @Override
            public String getExtension() {
                return "xml";
            }
        };

        public Runner createRunner(final RunOption aOption) {
            return new CmdRunner(aOption.getBaseDir()
                    , aOption.parameter()
            );
        }

        public String getExtension() {
            return this.toString();
        }
    }

}
