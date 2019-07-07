package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.compare.ColumnSetting;
import yo.dbunitcli.dataset.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.List;
import java.util.Properties;

abstract public class CommandLineOption {

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File("").getAbsoluteFile();

    @Option(name = "-resultType", usage = "csv | xlsx : default csv")
    private String resultType = "csv";

    @Option(name = "-setting", usage = "file define comparison settings", required = true)
    private File setting;

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-jdbcProperties", usage = "user connect database key[url,user,pass]")
    private File jdbcProperties;

    private Properties jdbcProp;

    private ColumnSetting.Builder comparisonKeys = ColumnSetting.builder();

    private ColumnSetting.Builder excludeColumns = ColumnSetting.builder();

    public String getEncoding() {
        return this.encoding;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public File getSetting() {
        return setting;
    }

    public ColumnSetting getComparisonKeys() {
        return this.comparisonKeys.build();
    }

    public ColumnSetting getExcludeColumns() {
        return this.excludeColumns.build();
    }

    public IDataSetWriter writer() {
        if (DataSourceType.XLSX.isEqual(this.resultType)) {
            return new XlsxDataSetWriter(this.getResultDir());
        }
        return new CsvDataSetWriterWrapper(this.getResultDir(), this.outputEncoding);
    }

    public ComparableDataSetLoader getComparableDataSetLoader() {
        if (this.jdbcProp != null) {
            return new ComparableDataSetLoader(this.jdbcProp.get("url").toString(), this.jdbcProp.get("user").toString(), this.jdbcProp.get("pass").toString());
        }
        return new ComparableDataSetLoader();
    }

    public void parse(String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            throw e;
        }
        assertDirectoryExists(parser);
        populateSettings(parser);
    }

    abstract protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException;

    protected void assertFileParameter(CmdLineParser parser, String source, File dir, String s) throws CmdLineException {
        final DataSourceType dataSourceType = DataSourceType.fromString(source);
        if (dataSourceType.isNeedDir()) {
            if (!dir.exists() || !dir.isDirectory()) {
                throw new CmdLineException(parser, s + " is not exist directory", new IllegalArgumentException(dir.toString()));
            }
        } else {
            if (!dir.exists() || !dir.isFile()) {
                throw new CmdLineException(parser, s + " is not exist file", new IllegalArgumentException(dir.toString()));
            }
        }
        if (dataSourceType == DataSourceType.TABLE || dataSourceType == DataSourceType.SQL) {
            if (this.jdbcProperties == null) {
                throw new CmdLineException(parser, dataSourceType + " need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new CmdLineException(parser, this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
            }
        }
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            if (this.jdbcProperties != null) {
                this.jdbcProp = new Properties();
                this.jdbcProp.load(new FileInputStream(this.jdbcProperties));
            }
            JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(this.setting), "windows-31j"));
            JsonObject setting = jsonReader.read()
                    .asJsonObject();
            this.configureSetting(setting);
            this.configureCommonSetting(setting);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

    protected void configureCommonSetting(JsonObject setting) {
        if (!setting.containsKey("commonSettings")) {
            return;
        }
        setting.getJsonArray("commonSettings")
                .stream()
                .forEach(v -> {
                    JsonObject json = v.asJsonObject();
                    if (json.containsKey("exclude")) {
                        JsonArray excludeArray = json.getJsonArray("exclude");
                        List<String> columns = Lists.newArrayList();
                        for (int i = 0, j = excludeArray.size(); i < j; i++) {
                            columns.add(excludeArray.getString(i));
                        }
                        this.excludeColumns.addCommon(columns);
                    }
                    if (json.containsKey("keys")) {
                        JsonArray excludeArray = json.getJsonArray("keys");
                        List<String> columns = Lists.newArrayList();
                        for (int i = 0, j = excludeArray.size(); i < j; i++) {
                            columns.add(excludeArray.getString(i));
                        }
                        this.comparisonKeys.addCommon(columns);
                    }
                });
    }

    protected void configureSetting(JsonObject setting) {
        setting.getJsonArray("settings")
                .stream()
                .forEach(v -> {
                    JsonObject json = v.asJsonObject();
                    if (json.containsKey("name")) {
                        String file = json.getString("name");
                        ColumnSetting.Strategy strategy = ColumnSetting.Strategy.BY_NAME;
                        this.addComparisonKeys(strategy, json, file);
                        this.addExcludeColumns(strategy, json, file);
                    } else if (json.containsKey("pattern")) {
                        String file = json.getString("pattern");
                        ColumnSetting.Strategy strategy = ColumnSetting.Strategy.PATTERN;
                        this.addComparisonKeys(strategy, json, file);
                        this.addExcludeColumns(strategy, json, file);
                    }
                });
    }

    protected void addExcludeColumns(ColumnSetting.Strategy strategy, JsonObject json, String file) {
        if (json.containsKey("exclude")) {
            JsonArray excludeArray = json.getJsonArray("exclude");
            List<String> columns = Lists.newArrayList();
            for (int i = 0, j = excludeArray.size(); i < j; i++) {
                columns.add(excludeArray.getString(i));
            }
            excludeColumns.add(strategy, file, columns);
        }
    }

    protected void addComparisonKeys(ColumnSetting.Strategy strategy, JsonObject json, String file) {
        if (json.containsKey("keys")) {
            JsonArray keyArray = json.getJsonArray("keys");
            List<String> keys = Lists.newArrayList();
            for (int i = 0, j = keyArray.size(); i < j; i++) {
                keys.add(keyArray.getString(i));
            }
            comparisonKeys.add(strategy, file, keys);
        }
    }

}
