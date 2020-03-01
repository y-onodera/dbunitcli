package yo.dbunitcli.application;

import com.google.common.io.Files;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.ErrorManager;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateOption extends ConvertOption {

    @Option(name = "-template", usage = "template file. encoding must be UTF-8. generate file convert outputEncoding", required = true)
    private File template;

    @Option(name = "-templateGroup", usage = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @Option(name = "-resultPath", usage = "Path to generate file", required = true)
    private String resultPath;

    @Option(name = "-unit", usage = "generate per record or table or dataset")
    private String unit = "record";

    private STGroup stGroup;

    private String templateString;

    public GenerateOption() {
        this(Parameter.none());
    }

    public GenerateOption(Parameter param) {
        super(param);
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
        for (String tableName : dataSet.getTableNames()) {
            param.put(tableName, dataSet.getTable(tableName).toMap(true));
        }
        return Stream.of(param);
    }

    public GenerateUnit getUnit() {
        return GenerateUnit.valueOf(this.unit.toUpperCase());
    }

    public String resultPath(Map<String, Object> param) {
        ST resultPath = new ST(this.stGroup, this.getResultPath());
        param.forEach(resultPath::add);
        return resultPath.render();
    }

    public void write(File resultFile, Map<String, Object> param) throws IOException {
        ST result = new ST(this.stGroup, this.templateString());
        param.forEach(result::add);
        result.write(resultFile, ErrorManager.DEFAULT_ERROR_LISTENER, this.getOutputEncoding());
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public String templateString() {
        return this.templateString;
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        this.stGroup = this.createSTGroup(this.templateGroup);
        try {
            this.templateString = Files.asCharSource(this.template, Charset.forName(getEncoding()))
                    .read();
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        super.assertDirectoryExists(parser);
        if (!this.template.exists() || !this.template.isFile()) {
            throw new CmdLineException(parser, this.template + " is not exist file"
                    , new IllegalArgumentException(this.template.toString()));
        }
    }

    public enum GenerateUnit {
        RECORD, TABLE, DATASET
    }
}
