package yo.dbunitcli.application;

import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.Files;
import yo.dbunitcli.resource.poi.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public record GenerateOption(
        BaseOption base
        , GenerateType generateType
        , ParameterUnit unit
        , DBConverter.Operation operation
        , String sqlFilePrefix
        , String sqlFileSuffix
        , boolean commit
        , File template
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
        new CommandLineParser("result").parseArgument(args, dto.getConvertResult());
        return dto;
    }

    private static GenerateType getGenerateType(final GenerateDto dto) {
        return dto.getGenerateType() != null ? dto.getGenerateType() : GenerateType.txt;
    }

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        this(new BaseOption(resultFile, dto, param)
                , GenerateOption.getGenerateType(dto)
                , GenerateOption.getGenerateType(dto).isFixedTemplate()
                        ? GenerateOption.getGenerateType(dto).getFixedUnit()
                        : dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record
                , dto.getOperation()
                , Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : ""
                , Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : ""
                , !Strings.isNotEmpty(dto.getCommit()) || Boolean.parseBoolean(dto.getCommit())
                , Strings.isNotEmpty(dto.getTemplate()) ? new File(dto.getTemplate()) : null
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , new DataSetLoadOption("src", dto.getSrcData())
                , new TemplateRenderOption("template", dto.getTemplateOption())
        );
    }

    public String templateString() {
        return this.generateType.getTemplateString(this);
    }

    public Stream<Parameter> parameterStream() {
        this.getParameter().getMap().put("commit", this.commit);
        return this.unit().loadStream(this.getComparableDataSetLoader(), this.dataSetParam());
    }

    public String resultPath(final Parameter param) {
        return this.templateOption.getTemplateRender().render(this.getResultPath(), param.getMap());
    }

    public File getResultDir() {
        return this.getConvertResult().resultDir();
    }

    public void write(final File resultFile, final Parameter param) throws IOException {
        this.generateType().write(this, resultFile, param.getMap());
    }

    @Override
    public GenerateDto toDto() {
        return GenerateOption.toDto(this.toArgs(true));
    }

    @Override
    public String getResultPath() {
        if (this.generateType() == GenerateType.sql) {
            final String tableName = this.templateOption.getTemplateRender().getAttributeName("tableName");
            return CommandLineOption.super.getResultPath() + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
        }
        return CommandLineOption.super.getResultPath();
    }

    @Override
    public BaseOption base() {
        return this.base;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.put("-generateType", this.generateType, GenerateType.class);
        final CommandLineArgs srcComponent = this.srcData.toCommandLineArgs();
        if (result.hasValue("-generateType")) {
            final GenerateType resultGenerateType = GenerateType.valueOf(result.get("-generateType"));
            if (!resultGenerateType.isFixedTemplate()) {
                result.put("-unit", this.unit, ParameterUnit.class);
                result.putFile("-template", this.template, true);
            }
            switch (resultGenerateType) {
                case sql -> {
                    result.put("-commit", Boolean.toString(this.commit));
                    result.put("-op", this.operation, DBConverter.Operation.class);
                    result.put("-sqlFilePrefix", this.sqlFilePrefix);
                    result.put("-sqlFileSuffix", this.sqlFileSuffix);
                    srcComponent.remove("-useJdbcMetaData");
                }
                case settings -> {
                    srcComponent.remove("-useJdbcMetaData");
                    srcComponent.remove("-loadData");
                }
                case xls, xlsx -> result.put("-outputEncoding", this.outputEncoding);
            }
            result.addComponent("srcData", srcComponent);
            if (!resultGenerateType.isFixedTemplate()) {
                result.addComponent("templateOption", this.templateOption.toCommandLineArgs());
            }
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
                if (option.template == null || !option.template.exists() || !option.template.isFile()) {
                    throw new AssertionError(option.template + " is not exist file"
                            , new IllegalArgumentException(String.valueOf(option.template)));
                }
                return Files.read(option.template, option.templateOption.encoding());
            }
        },
        xlsx {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.templateParameterAttribute())
                        .setFormulaProcess(option.templateOption.formulaProcess())
                        .build()
                        .render(option.template, resultFile, param);
            }
        },
        xls {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.templateParameterAttribute())
                        .build()
                        .render(option.template, resultFile, param);
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
                return Files.readClasspathResource("settings/settingTemplate.txt");
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
                return Files.readClasspathResource(option.getSqlTemplate());
            }

            @Override
            protected STGroup getStGroup() {
                return new TemplateRender.Builder()
                        .setTemplateParameterAttribute(null)
                        .build()
                        .createSTGroup("sql/sqlTemplate.stg");
            }
        };

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
