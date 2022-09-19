package yo.dbunitcli.application;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.TemplateRenderOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.consumer.DBConsumer;
import yo.dbunitcli.resource.Files;
import yo.dbunitcli.resource.poi.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateOption extends CommandLineOption {

    @Option(name = "-generateType")
    private GenerateType generateType = GenerateType.txt;

    @Option(name = "-unit")
    private GenerateUnit unit = GenerateUnit.record;

    @Option(name = "-commit", usage = "default commit;whether commit or not generate sql")
    private String commit = "true";

    @Option(name = "-sqlFileSuffix", usage = "generate sqlFile fileName suffix")
    private String sqlFileSuffix = "";

    @Option(name = "-sqlFilePrefix", usage = "generate sqlFile fileName prefix")
    private String sqlFilePrefix = "";

    @Option(name = "-op")
    private DBConsumer.Operation operation;

    @Option(name = "-outputEncoding", usage = "output file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-template", usage = "template file")
    private File template;

    private final DataSetLoadOption src = new DataSetLoadOption("src");

    private final TemplateRenderOption templateOption = new TemplateRenderOption("template");

    private String templateString;

    public GenerateOption() {
        this(Parameter.none());
    }

    public GenerateOption(Parameter param) {
        super(param);
    }

    public String templateString() {
        return this.templateString;
    }

    public GenerateUnit getUnit() {
        return this.unit;
    }

    public GenerateType getGenerateType() {
        return this.generateType;
    }

    public Stream<Map<String, Object>> parameterStream() throws DataSetException {
        this.getParameter().getMap().put("commit", Boolean.valueOf(this.commit));
        return this.getUnit().parameterStream(this.getParameter().getMap(), this.targetDataSet());
    }

    public String resultPath(Map<String, Object> param) {
        return this.templateOption.getTemplateRender().render(this.getResultPath(), param);
    }

    public void write(File resultFile, Map<String, Object> param) throws IOException {
        this.getGenerateType().write(this, resultFile, param);
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.getConsumerOption().parseArgument(expandArgs);
        this.src.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.getGenerateType().populateSettings(this, parser);
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.src.createOptionParam(args));
        result.put("-generateType", this.generateType, GenerateType.class);
        if (result.hasValue("-generateType")
                && GenerateType.valueOf(result.get("-generateType")) == GenerateType.txt
                || GenerateType.valueOf(result.get("-generateType")) == GenerateType.xlsx) {
            result.put("-unit", this.unit, GenerateUnit.class);
            result.putFile("-template", this.template, true);
        }
        if (result.hasValue("-generateType") && GenerateType.valueOf(result.get("-generateType")) == GenerateType.sql) {
            result.put("-commit", this.commit);
            result.put("-op", this.operation, DBConsumer.Operation.class);
            result.put("-sqlFilePrefix", this.sqlFilePrefix);
            result.put("-sqlFileSuffix", this.sqlFileSuffix);
        }
        result.putAll(this.templateOption.createOptionParam(args));
        if (result.hasValue("-generateType")
                && !(GenerateType.valueOf(result.get("-generateType")) == GenerateType.sql
                || GenerateType.valueOf(result.get("-generateType")) == GenerateType.settings)) {
            result.put("-outputEncoding", this.outputEncoding);
        }
        return result;
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        ComparableDataSetParam.Builder builder = this.src.getParam();
        if (this.getGenerateType() == GenerateType.settings) {
            builder.setUseJdbcMetaData(true);
            builder.setLoadData(false);
        } else if (this.getGenerateType() == GenerateType.sql) {
            builder.setUseJdbcMetaData(true);
        }
        return this.getComparableDataSetLoader().loadDataSet(builder.build());
    }

    protected String getSqlTemplate() {
        switch (this.operation) {
            case INSERT:
                return "sql/insertTemplate.txt";
            case DELETE:
                return "sql/deleteTemplate.txt";
            case UPDATE:
                return "sql/updateTemplate.txt";
            case CLEAN_INSERT:
                return "sql/cleanInsertTemplate.txt";
            default:
                return "sql/deleteInsertTemplate.txt";
        }
    }

    protected String getResultPath() {
        if (getGenerateType() == GenerateType.sql) {
            String tableName = this.templateOption.getTemplateRender().getAttributeName("tableName");
            return super.getResultPath() + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
        }
        return super.getResultPath();
    }

    public File getResultDir() {
        return this.getConsumerOption().getResultDir();
    }

    public enum GenerateUnit {
        record {
            @Override
            public Stream<Map<String, Object>> parameterStream(Map<String, Object> map, ComparableDataSet dataSet) throws DataSetException {
                return dataSet.toMap(true).stream()
                        .flatMap(it -> ((List<Map<String, Object>>) it.get("row")).stream()
                                .map(row -> {
                                    Map<String, Object> result = Maps.newHashMap();
                                    result.putAll(it);
                                    result.put("row", row);
                                    result.put("_paramMap", map);
                                    return result;
                                })
                        );
            }
        },

        table {
            @Override
            public Stream<Map<String, Object>> parameterStream(Map<String, Object> map, ComparableDataSet dataSet) throws
                    DataSetException {
                return Stream.of(dataSet.getTableNames())
                        .map(it -> {
                            try {
                                Map<String, Object> param = new HashMap<>();
                                param.put("_paramMap", map);
                                ComparableTable table = dataSet.getTable(it);
                                param.put("tableName", it);
                                param.put("primaryKeys", table.getTableMetaData().getPrimaryKeys());
                                param.put("columns", table.getTableMetaData().getColumns());
                                param.put("columnsExcludeKey", table.getColumnsExcludeKey());
                                param.put("rows", table.toMap());
                                return param;
                            } catch (DataSetException e) {
                                throw new AssertionError(e);
                            }
                        });
            }
        },
        dataset;

        public Stream<Map<String, Object>> parameterStream(Map<String, Object> map, ComparableDataSet dataSet) throws DataSetException {
            Map<String, Object> param = new HashMap<>();
            param.put("_paramMap", map);
            param.put("dataSet", dataSet.toMap(true));
            return Stream.of(param);
        }
    }

    public enum GenerateType {
        txt,
        xlsx {
            @Override
            protected void write(GenerateOption option, File resultFile, Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.getTemplateParameterAttribute())
                        .build()
                        .render(option.template, resultFile, param);
            }
        },
        settings {
            @Override
            protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
                option.unit = GenerateUnit.dataset;
                try {
                    option.templateString = Files.readClasspathResource("settings/settingTemplate.txt");
                } catch (IOException | URISyntaxException e) {
                    throw new CmdLineException(parser, e);
                }
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
            protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
                option.unit = GenerateUnit.table;
                try {
                    option.templateString = Files.readClasspathResource(option.getSqlTemplate());
                } catch (IOException | URISyntaxException e) {
                    throw new CmdLineException(parser, e);
                }
            }

            @Override
            protected STGroup getStGroup() {
                return new TemplateRender.Builder()
                        .setTemplateParameterAttribute(null)
                        .build()
                        .createSTGroup("sql/sqlTemplate.stg");
            }
        };

        protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
            File template = option.template;
            if (!template.exists() || !template.isFile()) {
                throw new CmdLineException(parser, template + " is not exist file"
                        , new IllegalArgumentException(template.toString()));
            }
            if (this == GenerateType.txt) {
                try {
                    option.templateString = Files.read(template, option.templateOption.getTemplateEncoding());
                } catch (IOException e) {
                    throw new CmdLineException(parser, e);
                }
            }
        }

        protected STGroup getStGroup() {
            return null;
        }

        protected void write(GenerateOption option, File resultFile, Map<String, Object> param) throws IOException {
            option.templateOption.getTemplateRender().write(getStGroup()
                    , option.templateString()
                    , param
                    , resultFile
                    , option.outputEncoding);
        }
    }
}
