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

public class GenerateOption extends CommandLineOption<GenerateDto> {

    private final DataSetLoadOption srcData;
    private final ParameterUnit unit;
    private final TemplateRenderOption templateOption;
    private final GenerateType generateType;
    private final DBConverter.Operation operation;
    private final boolean commit;
    private final String sqlFileSuffix;
    private final String sqlFilePrefix;
    private final String outputEncoding;
    private final File template;

    public static GenerateDto toDto(final String[] args) {
        final GenerateDto dto = new GenerateDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("src").parseArgument(args, dto.getSrcData());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateOption());
        new CommandLineParser("result").parseArgument(args, dto.getConvertResult());
        return dto;
    }

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        super(resultFile, dto, param);
        if (dto.getGenerateType() != null) {
            this.generateType = dto.getGenerateType();
        } else {
            this.generateType = GenerateType.txt;
        }
        if (this.generateType.isFixedTemplate()) {
            this.unit = this.generateType.getFixedUnit();
        } else if (dto.getUnit() != null) {
            this.unit = dto.getUnit();
        } else {
            this.unit = ParameterUnit.record;
        }
        this.operation = dto.getOperation();
        if (Strings.isNotEmpty(dto.getSqlFilePrefix())) {
            this.sqlFilePrefix = dto.getSqlFilePrefix();
        } else {
            this.sqlFilePrefix = "";
        }
        if (Strings.isNotEmpty(dto.getSqlFileSuffix())) {
            this.sqlFileSuffix = dto.getSqlFileSuffix();
        } else {
            this.sqlFileSuffix = "";
        }
        if (Strings.isNotEmpty(dto.getCommit())) {
            this.commit = Boolean.parseBoolean(dto.getCommit());
        } else {
            this.commit = true;
        }
        if (Strings.isNotEmpty(dto.getTemplate())) {
            this.template = new File(dto.getTemplate());
        } else {
            this.template = null;
        }
        if (Strings.isNotEmpty(dto.getOutputEncoding())) {
            this.outputEncoding = dto.getOutputEncoding();
        } else {
            this.outputEncoding = "UTF-8";
        }
        this.srcData = new DataSetLoadOption("src", dto.getSrcData());
        this.templateOption = new TemplateRenderOption("template", dto.getTemplateOption());
    }

    public String templateString() {
        return this.generateType.getTemplateString(this);
    }

    public ParameterUnit getUnit() {
        return this.unit;
    }

    public GenerateType getGenerateType() {
        return this.generateType;
    }

    public Stream<Parameter> parameterStream() {
        this.getParameter().getMap().put("commit", this.commit);
        return this.getUnit().loadStream(this.getComparableDataSetLoader(), this.dataSetParam());
    }

    public String resultPath(final Parameter param) {
        return this.templateOption.getTemplateRender().render(this.getResultPath(), param.getMap());
    }

    public File getResultDir() {
        return this.getConvertResult().getResultDir();
    }

    public void write(final File resultFile, final Parameter param) throws IOException {
        this.getGenerateType().write(this, resultFile, param.getMap());
    }

    @Override
    public GenerateDto toDto() {
        return GenerateOption.toDto(this.toArgs(true));
    }

    @Override
    protected String getResultPath() {
        if (this.getGenerateType() == GenerateType.sql) {
            final String tableName = this.templateOption.getTemplateRender().getAttributeName("tableName");
            return super.getResultPath() + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
        }
        return super.getResultPath();
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.put("-generateType", this.generateType, GenerateType.class);
        if (result.hasValue("-generateType")) {
            final GenerateType resultGenerateType = GenerateType.valueOf(result.get("-generateType"));
            if (!resultGenerateType.isFixedTemplate()) {
                result.put("-unit", this.unit, ParameterUnit.class);
                result.putFile("-template", this.template, true);
            } else if (resultGenerateType == GenerateType.sql) {
                result.put("-commit", Boolean.toString(this.commit));
                result.put("-op", this.operation, DBConverter.Operation.class);
                result.put("-sqlFilePrefix", this.sqlFilePrefix);
                result.put("-sqlFileSuffix", this.sqlFileSuffix);
            }
            if (!(resultGenerateType == GenerateType.xls || resultGenerateType == GenerateType.xlsx)) {
                result.put("-outputEncoding", this.outputEncoding);
            }
        }
        result.addComponent("srcData", this.srcData.toCommandLineArgs());
        result.addComponent("templateOption", this.templateOption.toCommandLineArgs());
        return result;
    }

    public ComparableDataSetParam dataSetParam() {
        final ComparableDataSetParam.Builder builder = this.srcData.getParam();
        if (this.getGenerateType() == GenerateType.settings) {
            builder.setUseJdbcMetaData(true);
            builder.setLoadData(false);
        } else if (this.getGenerateType() == GenerateType.sql) {
            builder.setUseJdbcMetaData(true);
        }
        return builder.build();
    }

    protected String getSqlTemplate() {
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
                return Files.read(option.template, option.templateOption.getTemplateEncoding());
            }
        },
        xlsx {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.getTemplateParameterAttribute())
                        .setFormulaProcess(option.templateOption.isFormulaProcess())
                        .build()
                        .render(option.template, resultFile, param);
            }
        },
        xls {
            @Override
            protected void write(final GenerateOption option, final File resultFile, final Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.getTemplateParameterAttribute())
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
