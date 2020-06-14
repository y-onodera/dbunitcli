package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
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

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateOption extends ConvertOption {

    @Option(name = "-template", usage = "template file. encoding must be UTF-8. generate file convert outputEncoding")
    private File template;

    @Option(name = "-templateGroup", usage = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @Option(name = "-templateEncoding", usage = "template txt file encoding")
    private String templateEncoding = System.getProperty("file.encoding");

    @Option(name = "-resultPath", usage = "Path to generate file", required = true)
    private String resultPath;

    @Option(name = "-unit", usage = "record | table | dataset :generate per record or table or dataset")
    private String unit = "record";

    @Option(name = "-generateType", usage = "txt | xlsx | settings | sql :generate planTxt or xlsx")
    private String generateType = "txt";

    private STGroup stGroup;

    private String templateString;

    public GenerateOption() {
        this(Parameter.none());
    }

    public GenerateOption(Parameter param) {
        super(param);
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public File getTemplate() {
        return this.template;
    }

    public String getTemplateEncoding() {
        return templateEncoding;
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
        final ComparableDataSet dataSet = this.targetDataSet();
        switch (this.getUnit()) {
            case RECORD:
                return dataSet.toMap(true).stream().map(it -> {
                    it.put("_paramMap", getParameter().getMap());
                    return it;
                });
            case TABLE:
                return Stream.of(dataSet.getTableNames())
                        .map(it -> {
                            try {
                                Map<String, Object> param = new HashMap<>();
                                param.put("_paramMap", getParameter().getMap());
                                ComparableTable table = dataSet.getTable(it);
                                param.put("tableName", it);
                                param.put("columns", table.getTableMetaData().getColumns());
                                param.put("primaryKeys", table.getTableMetaData().getPrimaryKeys());
                                param.put("rows", table.toMap());
                                return param;
                            } catch (DataSetException e) {
                                throw new AssertionError(e);
                            }
                        });
            case DATASET:
        }
        Map<String, Object> param = new HashMap<>();
        param.put("_paramMap", getParameter().getMap());
        List<String> tableNames = Lists.newArrayList();
        param.put("dataSet", dataSet.toMap(true));
        return Stream.of(param);
    }

    public String resultPath(Map<String, Object> param) {
        ST resultPath = new ST(this.stGroup, this.getResultPath());
        param.forEach(resultPath::add);
        return resultPath.render();
    }

    public void write(File resultFile, Map<String, Object> param) throws IOException {
        if (this.getGenerateType() == GenerateType.TXT || this.getGenerateType() == GenerateType.SETTINGS) {
            ST result = new ST(this.stGroup, this.templateString());
            param.forEach(result::add);
            result.write(resultFile, ErrorManager.DEFAULT_ERROR_LISTENER, this.getOutputEncoding());
        } else {
            try (InputStream is = new FileInputStream(this.getTemplate())) {
                try (OutputStream os = new FileOutputStream(resultFile)) {
                    Context context = new Context();
                    context.putVar("param", param);
                    JxlsHelper.getInstance().processTemplate(is, os, context);
                }
            }
        }
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        if (this.getGenerateType() == GenerateType.SETTINGS) {
            this.unit = "dataset";
            this.setOutputEncoding("UTF-8");
            this.setUseJdbcMetaData("true");
            this.setLoadData("false");
            this.stGroup = this.createSTGroup(new File("settingTemplate.stg"));
            try {
                this.templateString = Resources.asCharSource(this.getClass()
                                .getClassLoader()
                                .getResource("settingTemplate.txt")
                                .toURI()
                                .toURL()
                        , Charset.forName("UTF-8"))
                        .read();
            } catch (IOException | URISyntaxException e) {
                throw new CmdLineException(parser, e);
            }
        } else {
            this.stGroup = this.createSTGroup(this.templateGroup);
            if (this.getGenerateType() == GenerateType.TXT) {
                try {
                    this.templateString = Files.asCharSource(this.template, Charset.forName(this.getTemplateEncoding()))
                            .read();
                } catch (IOException e) {
                    throw new CmdLineException(parser, e);
                }
            }
        }
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        super.assertDirectoryExists(parser);
        if (this.getGenerateType() != GenerateType.SETTINGS) {
            if (!this.template.exists() || !this.template.isFile()) {
                throw new CmdLineException(parser, this.template + " is not exist file"
                        , new IllegalArgumentException(this.template.toString()));
            }
        }
    }

    public enum GenerateUnit {
        RECORD, TABLE, DATASET
    }

    public enum GenerateType {
        XLSX, TXT, SETTINGS;

        static GenerateType fromString(String name) {
            return Stream.of(GenerateType.values())
                    .filter(it -> it.name().equals(name.toUpperCase()))
                    .findFirst()
                    .get();
        }
    }
}
