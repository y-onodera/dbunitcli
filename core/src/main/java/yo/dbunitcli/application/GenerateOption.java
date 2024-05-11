package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.Files;
import yo.dbunitcli.resource.poi.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateOption extends CommandLineOption<GenerateDto> {

    private final DataSetLoadOption src;
    private final TemplateRenderOption templateOption;
    private final GenerateType generateType;
    private final DBConverter.Operation operation;
    private final GenerateUnit unit;
    private final boolean commit;
    private final String sqlFileSuffix;
    private final String sqlFilePrefix;
    private final String outputEncoding;
    private final File template;

    public static GenerateDto toDto(final String[] args) {
        final GenerateDto dto = new GenerateDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        new CommandLineParser("src").parseArgument(args, dto.getDataSetLoad());
        new CommandLineParser("template").parseArgument(args, dto.getTemplateRender());
        new CommandLineParser("result").parseArgument(args, dto.getDataSetConverter());
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
            this.unit = GenerateUnit.record;
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
        this.src = new DataSetLoadOption("src", dto.getDataSetLoad());
        this.templateOption = new TemplateRenderOption("template", dto.getTemplateRender());
    }

    public String templateString() {
        return this.generateType.getTemplateString(this);
    }

    public GenerateUnit getUnit() {
        return this.unit;
    }

    public GenerateType getGenerateType() {
        return this.generateType;
    }

    public Stream<Map<String, Object>> parameterStream() {
        this.getParameter().getMap().put("commit", this.commit);
        return this.getUnit().parameterStream(this.getParameter().getMap(), this.targetDataSet());
    }

    public String resultPath(final Map<String, Object> param) {
        return this.templateOption.getTemplateRender().render(this.getResultPath(), param);
    }

    public File getResultDir() {
        return this.getConverterOption().getResultDir();
    }

    public void write(final File resultFile, final Map<String, Object> param) throws IOException {
        this.getGenerateType().write(this, resultFile, param);
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
        result.putAll(this.src.toCommandLineArgs());
        result.put("-generateType", this.generateType, GenerateType.class);
        if (result.hasValue("-generateType")
                && GenerateType.valueOf(result.get("-generateType")) == GenerateType.txt
                || GenerateType.valueOf(result.get("-generateType")) == GenerateType.xlsx) {
            result.put("-unit", this.unit, GenerateUnit.class);
            result.putFile("-template", this.template, true);
        }
        if (result.hasValue("-generateType") && GenerateType.valueOf(result.get("-generateType")) == GenerateType.sql) {
            result.put("-commit", Boolean.toString(this.commit));
            result.put("-op", this.operation, DBConverter.Operation.class);
            result.put("-sqlFilePrefix", this.sqlFilePrefix);
            result.put("-sqlFileSuffix", this.sqlFileSuffix);
        }
        result.putAll(this.templateOption.toCommandLineArgs());
        if (result.hasValue("-generateType")
                && !(GenerateType.valueOf(result.get("-generateType")) == GenerateType.sql
                || GenerateType.valueOf(result.get("-generateType")) == GenerateType.settings)) {
            result.put("-outputEncoding", this.outputEncoding);
        }
        return result;
    }

    public ComparableDataSet targetDataSet() {
        final ComparableDataSetParam.Builder builder = this.src.getParam();
        if (this.getGenerateType() == GenerateType.settings) {
            builder.setUseJdbcMetaData(true);
            builder.setLoadData(false);
        } else if (this.getGenerateType() == GenerateType.sql) {
            builder.setUseJdbcMetaData(true);
        }
        return this.getComparableDataSetLoader().loadDataSet(builder.build());
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

    public enum GenerateUnit {
        record {
            @Override
            public Stream<Map<String, Object>> parameterStream(final Map<String, Object> map, final ComparableDataSet dataSet) {
                return dataSet.toMap(true)
                        .flatMap(it -> ((List<Map<String, Object>>) it.get("rows")).stream()
                                .map(row -> {
                                    final Map<String, Object> result = new HashMap<>(it);
                                    result.put("row", row);
                                    result.put("_paramMap", map);
                                    return result;
                                })
                        );
            }
        },

        table {
            @Override
            public Stream<Map<String, Object>> parameterStream(final Map<String, Object> map, final ComparableDataSet dataSet) {
                try {
                    return Stream.of(dataSet.getTableNames())
                            .map(it -> {
                                final Map<String, Object> param = new HashMap<>();
                                param.put("_paramMap", map);
                                final ComparableTable table = dataSet.getTable(it);
                                param.put("tableName", it);
                                param.put("primaryKeys", table.getTableMetaData().getPrimaryKeys());
                                param.put("columns", table.getTableMetaData().getColumns());
                                param.put("columnsExcludeKey", table.getColumnsExcludeKey());
                                param.put("rows", table.toMap());
                                return param;
                            });
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
            }
        },
        dataset;

        public Stream<Map<String, Object>> parameterStream(final Map<String, Object> map, final ComparableDataSet dataSet) {
            final Map<String, Object> param = new HashMap<>();
            param.put("_paramMap", map);
            param.put("dataSet", dataSet.toMap(true)
                    .collect(Collectors.toMap(it -> it.get("tableName").toString(), it -> it, (old, other) -> other, LinkedHashMap::new)));
            return Stream.of(param);
        }
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
            public GenerateUnit getFixedUnit() {
                return GenerateUnit.dataset;
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
            public GenerateUnit getFixedUnit() {
                return GenerateUnit.table;
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

        public boolean isFixedTemplate() {
            return false;
        }

        public GenerateUnit getFixedUnit() {
            return null;
        }

        public String getTemplateString(final GenerateOption option) {
            return null;
        }
    }
}
