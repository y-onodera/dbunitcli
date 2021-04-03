package yo.dbunitcli.application;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.dbunit.dataset.DataSetException;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.ErrorManager;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.writer.DBDataSetWriter;
import yo.dbunitcli.resource.poi.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateOption extends ConvertOption {

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
        return this.getTemplateRender().render(this.getResultPath(), param);
    }

    public void write(File resultFile, Map<String, Object> param) throws IOException {
        this.getGenerateType().write(this, resultFile, param);
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        this.getGenerateType().populateSettings(this, parser);
    }

    protected String getSqlTemplate() {
        switch (DBDataSetWriter.Operation.valueOf(this.getOperation())) {
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

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        super.assertDirectoryExists(parser);
        if (this.getGenerateType() != GenerateType.SETTINGS
                && this.getGenerateType() != GenerateType.SQL
        ) {
            if (!this.getTemplate().exists() || !this.getTemplate().isFile()) {
                throw new CmdLineException(parser, this.getTemplate() + " is not exist file"
                        , new IllegalArgumentException(this.getTemplate().toString()));
            }
        }
    }

    protected String readClassPathResource(String path) throws IOException, URISyntaxException {
        return Resources.asCharSource(this.getClass()
                        .getClassLoader()
                        .getResource(path)
                        .toURI()
                        .toURL()
                , Charset.forName("UTF-8"))
                .read();
    }

    protected String getResultSqlFilePath() {
        String tableName = this.getTemplateRender().getAttributeName("tableName");
        return this.getResultPath() + "/" + this.sqlFilePrefix + tableName + this.sqlFileSuffix + ".sql";
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
                        .setTemplateParameterAttribute(option.getTemplateParameterAttribute())
                        .build()
                        .render(option.getTemplate(), resultFile, param);
            }
        },
        SETTINGS {
            @Override
            protected void populateSettings(GenerateOption option, CmdLineParser parser) throws CmdLineException {
                option.unit = "dataset";
                option.setOutputEncoding("UTF-8");
                option.setUseJdbcMetaData("true");
                option.setLoadData("false");
                try {
                    option.templateString = option.readClassPathResource("settings/settingTemplate.txt");
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
                option.setResultPath(option.getResultSqlFilePath());
                option.unit = "table";
                option.setUseJdbcMetaData("true");
                try {
                    option.templateString = option.readClassPathResource(option.getSqlTemplate());
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
            if (this == GenerateType.TXT) {
                try {
                    option.templateString = option.getTemplateRender()
                            .toString(option.getTemplate());
                } catch (IOException e) {
                    throw new CmdLineException(parser, e);
                }
            }
        }

        protected STGroup getStGroup() {
            return null;
        }

        protected void write(GenerateOption option, File resultFile, Map<String, Object> param) throws IOException {
            option.getTemplateRender().write(getStGroup()
                    , option.templateString()
                    , param
                    , resultFile
                    , option.getOutputEncoding());
        }
    }

}
