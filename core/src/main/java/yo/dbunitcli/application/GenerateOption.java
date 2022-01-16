package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.application.component.DataSetLoadOption;
import yo.dbunitcli.application.component.TemplateRenderOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.writer.DBDataSetWriter;
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

    @Option(name = "-unit", usage = "record | table | dataset :generate per record or table or dataset")
    private String unit = "record";

    @Option(name = "-generateType", usage = "txt | xlsx | settings | sql :generate planTxt or xlsx")
    private String generateType = "txt";

    @Option(name = "-commit", usage = "default commit;whether commit or not generate sql")
    private String commit = "true";

    @Option(name = "-sqlFileSuffix", usage = "generate sqlFile fileName suffix")
    private String sqlFileSuffix = "";

    @Option(name = "-sqlFilePrefix", usage = "generate sqlFile fileName prefix")
    private String sqlFilePrefix = "";

    private DataSetLoadOption src = new DataSetLoadOption("src");

    private TemplateRenderOption templateOption = new TemplateRenderOption("");

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
        return GenerateUnit.valueOf(this.unit.toUpperCase());
    }

    public GenerateType getGenerateType() {
        return GenerateType.fromString(this.generateType);
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
    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.src.parseArgument(expandArgs);
        this.templateOption.parseArgument(expandArgs);
        this.getGenerateType().populateSettings(this, parser);
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        ComparableDataSetParam.Builder builder = this.src.getParam();
        if (this.getGenerateType() == GenerateType.SETTINGS) {
            builder.setUseJdbcMetaData(true);
            builder.setLoadData(false);
        } else if (this.getGenerateType() == GenerateType.SQL) {
            builder.setUseJdbcMetaData(true);
        }
        return this.getComparableDataSetLoader().loadDataSet(builder.build());
    }

    protected String getSqlTemplate() {
        switch (DBDataSetWriter.Operation.valueOf(this.getWriteOption().getJdbcOption().getOperation())) {
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
        if (getGenerateType() == GenerateType.SQL) {
            String tableName = this.templateOption.getTemplateRender().getAttributeName("tableName");
            return this.getWriteOption().getResultPath() + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
        }
        return this.getWriteOption().getResultPath();
    }

    public File getResultDir() {
        return this.getWriteOption().getResultDir();
    }

    public enum GenerateUnit {
        RECORD {
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

        TABLE {
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
        DATASET;

        public Stream<Map<String, Object>> parameterStream(Map<String, Object> map, ComparableDataSet dataSet) throws DataSetException {
            Map<String, Object> param = new HashMap<>();
            param.put("_paramMap", map);
            List<String> tableNames = Lists.newArrayList();
            param.put("dataSet", dataSet.toMap(true));
            return Stream.of(param);
        }
    }

    public enum GenerateType {
        TXT,
        XLSX {
            @Override
            protected void write(GenerateOption option, File resultFile, Map<String, Object> param) throws IOException {
                JxlsTemplateRender.builder()
                        .setTemplateParameterAttribute(option.templateOption.getTemplateParameterAttribute())
                        .build()
                        .render(option.templateOption.getTemplate(), resultFile, param);
            }
        },
        SETTINGS {
            @Override
            protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
                option.unit = "dataset";
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
        SQL {
            @Override
            protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
                option.unit = "table";
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

        static GenerateType fromString(String name) {
            return Stream.of(GenerateType.values())
                    .filter(it -> it.name().equals(name.toUpperCase()))
                    .findFirst()
                    .get();
        }

        protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
            File template = option.templateOption.getTemplate();
            if (!template.exists() || !template.isFile()) {
                throw new CmdLineException(parser, template + " is not exist file"
                        , new IllegalArgumentException(template.toString()));
            }
            if (this == GenerateType.TXT) {
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
            String outputEncoding = option.getWriteOption().getOutputEncoding();
            if (option.getGenerateType() == SETTINGS) {
                outputEncoding = "UTF-8";
            }
            option.templateOption.getTemplateRender().write(getStGroup()
                    , option.templateString()
                    , param
                    , resultFile
                    , outputEncoding);
        }
    }
}
