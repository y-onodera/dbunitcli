package yo.dbunitcli.application;

import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record GenerateOption(
        Parameter parameter
        , String resultDir
        , String resultPath
        , GenerateType generateType
        , ParameterUnit unit
        , DBConverter.Operation operation
        , String sqlFilePrefix
        , String sqlFileSuffix
        , boolean commit
        , String template
        , String outputEncoding
        , DataSetLoadOption srcData
        , TemplateRenderOption templateOption
) implements CommandLineOption<GenerateDto> {

    public static GenerateDto toDto(final String[] args) {
        final GenerateDto dto = new GenerateDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("src").parseArgument(args, dto.getSrcData());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateOption());
        return dto;
    }

    private static GenerateType getGenerateType(final GenerateDto dto) {
        return dto.getGenerateType() != null ? dto.getGenerateType() : GenerateType.txt;
    }

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        this(param
                , dto.getResultDir()
                , Optional.ofNullable(dto.getResultPath())
                        .filter(it -> !it.isEmpty())
                        .orElse(resultFile)
                , GenerateOption.getGenerateType(dto)
                , GenerateOption.getGenerateType(dto).isFixedTemplate()
                        ? GenerateOption.getGenerateType(dto).getFixedUnit()
                        : dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record
                , dto.getOperation()
                , Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : ""
                , Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : ""
                , !Strings.isNotEmpty(dto.getCommit()) || Boolean.parseBoolean(dto.getCommit())
                , dto.getTemplate()
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , new DataSetLoadOption("src", dto.getSrcData())
                , new TemplateRenderOption("template", dto.getTemplateOption())
        );
    }

    public String templateString() {
        return this.generateType.getTemplateString(this);
    }

    public Stream<Parameter> parameterStream() {
        this.parameter().getMap().put("commit", this.commit);
        return this.unit().loadStream(this.getComparableDataSetLoader(), this.dataSetParam());
    }

    public File resultFile(final Parameter param) {
        return new File(this.getResultDir(), this.resultPath(param));
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir());
    }

    public String resultPath(final Parameter param) {
        return this.templateOption.getTemplateRender().render(this.resultPath(), param.getMap());
    }

    @Override
    public String resultPath() {
        if (this.generateType() == GenerateType.sql) {
            final String tableName = this.templateOption.getTemplateRender().getAttributeName("tableName");
            return this.resultPath + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
        }
        return this.resultPath;
    }

    public void write(final File resultFile, final Parameter param) throws IOException {
        this.generateType().write(this, resultFile, param.getMap());
    }

    @Override
    public GenerateDto toDto() {
        return GenerateOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        final CommandLineArgsBuilder result = new CommandLineArgsBuilder();
        if (this.generateType == null) {
            return result;
        }
        result.put("-generateType", this.generateType, GenerateType.class);
        if (!this.generateType.isFixedTemplate()) {
            result.put("-unit", this.unit, ParameterUnit.class)
                    .putFile("-template", Strings.isNotEmpty(this.template) ? new File(this.template) : null, true);
        }
        final CommandLineArgsBuilder srcComponent = this.srcData.toCommandLineArgsBuilder();
        switch (this.generateType) {
            case sql -> {
                result.put("-commit", Boolean.toString(this.commit))
                        .put("-op", this.operation, DBConverter.Operation.class)
                        .put("-sqlFilePrefix", this.sqlFilePrefix)
                        .put("-sqlFileSuffix", this.sqlFileSuffix);
                srcComponent.remove("-src.useJdbcMetaData");
            }
            case settings -> {
                srcComponent.remove("-src.useJdbcMetaData")
                        .remove("-src.loadData");
            }
        }
        result.addComponent("srcData", srcComponent.build());
        if (!this.generateType.isFixedTemplate()) {
            result.addComponent("templateOption", this.templateOption.toCommandLineArgs());
        }
        result.putDir("-result", Strings.isNotEmpty(this.resultDir) ? new File(this.resultDir) : null)
                .put("-resultPath", this.resultPath);
        if (!(this.generateType.isAny(GenerateType.xlsx, GenerateType.xls))) {
            result.put("-outputEncoding", this.outputEncoding);
        }
        return result;
    }

    public ComparableDataSetParam dataSetParam() {
        final ComparableDataSetParam.Builder builder = this.srcData.getParam();
        if (this.generateType() == GenerateType.settings) {
            builder.setUseJdbcMetaData(true);
            builder.setLoadData(false);
        } else if (this.generateType() == GenerateType.sql) {
            builder.setUseJdbcMetaData(true);
        }
        return builder.build();
    }

    public File getTemplatePath() {
        return FileResources.searchTemplate(this.template());
    }

    private String getSqlTemplate() {
        return switch (this.operation) {
            case INSERT -> "sql/insertTemplate.txt";
            case DELETE -> "sql/deleteTemplate.txt";
            case UPDATE -> "sql/updateTemplate.txt";
            case CLEAN_INSERT -> "sql/cleanInsertTemplate.txt";
            default -> "sql/deleteInsertTemplate.txt";
        };
    }

    public enum GenerateType {
        txt {
            @Override
            public String getTemplateString(final GenerateOption option) {
                final File templatePath = option.getTemplatePath();
                if (templatePath == null || !templatePath.exists() || !templatePath.isFile()) {
                    throw new AssertionError(option.template + " is not exist file"
                            , new IllegalArgumentException(String.valueOf(option.template)));
                }
                return FileResources.read(templatePath, option.templateOption.encoding());
            }
        },
        xlsx {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.templateParameterAttribute())
                        .setFormulaProcess(option.templateOption.formulaProcess())
                        .build()
                        .render(option.getTemplatePath(), resultFile, param);
            }
        },
        xls {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.templateParameterAttribute())
                        .build()
                        .render(option.getTemplatePath(), resultFile, param);
            }
        },
        settings {
            @Override
            public boolean isFixedTemplate() {
                return true;
            }

            @Override
            public ParameterUnit getFixedUnit() {
                return ParameterUnit.dataset;
            }

            @Override
            public String getTemplateString(final GenerateOption option) {
                return FileResources.readClasspathResource("settings/settingTemplate.txt");
            }

            @Override
            protected STGroup getStGroup() {
                return new TemplateRender.Builder()
                        .setTemplateParameterAttribute(null)
                        .build()
                        .createSTGroup("settings/settingTemplate.stg");
            }
        },
        sql {
            @Override
            public boolean isFixedTemplate() {
                return true;
            }

            @Override
            public ParameterUnit getFixedUnit() {
                return ParameterUnit.table;
            }

            @Override
            public String getTemplateString(final GenerateOption option) {
                return FileResources.readClasspathResource(option.getSqlTemplate());
            }

            @Override
            protected STGroup getStGroup() {
                return new TemplateRender.Builder()
                        .setTemplateParameterAttribute(null)
                        .build()
                        .createSTGroup("sql/sqlTemplate.stg");
            }
        };

        public boolean isAny(final GenerateType... expects) {
            return Stream.of(expects).anyMatch(it -> it == this);
        }

        protected STGroup getStGroup() {
            return null;
        }

        protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
            option.templateOption.getTemplateRender().write(this.getStGroup()
                    , option.templateString()
                    , param
                    , resultFile
                    , option.outputEncoding);
        }

        protected boolean isFixedTemplate() {
            return false;
        }

        protected ParameterUnit getFixedUnit() {
            return null;
        }

        protected String getTemplateString(final GenerateOption option) {
            return null;
        }
    }
}
