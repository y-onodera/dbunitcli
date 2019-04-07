package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import yo.dbunitcli.compare.CompareSetting;
import yo.dbunitcli.dataset.*;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import javax.json.*;

public class CommandLineOptions {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptions.class);

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-old", usage = "directory old files at", required = true)
    private File oldDir;

    @Option(name = "-oldsource", usage = "csv | xls | xlsx : default csv")
    private String oldsource = "csv";

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

    @Option(name = "-newsource", usage = "csv | xls | xlsx : default csv")
    private String newsource = "csv";

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File("").getAbsoluteFile();

    @Option(name = "-setting", usage = "file define comparison settings", required = true)
    private File comparisonKeySetting;

    @Option(name = "-expect", usage = "expected diff")
    private File expected;

    @Option(name = "-resultType", usage = "csv | xlsx : default csv")
    private String resultType = "csv";

    private CompareSetting.Builder comparisonKeys = CompareSetting.builder();

    private CompareSetting.Builder excludeColumns = CompareSetting.builder();

    private ComparableDataSetLoader comparableDataSetLoader = new ComparableDataSetLoader();

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

    public String getEncoding() {
        return this.encoding;
    }

    public File getOldDir() {
        return this.oldDir;
    }

    public File getNewDir() {
        return this.newDir;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public CompareSetting getComparisonKeys() {
        return this.comparisonKeys.build();
    }

    public CompareSetting getExcludeColumns() {
        return this.excludeColumns.build();
    }

    public File getExpected() {
        return this.expected;
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        return this.comparableDataSetLoader.loadDataSet(this.getOldDir(), this.getEncoding(), this.oldsource, this.excludeColumns.build());
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        return this.comparableDataSetLoader.loadDataSet(this.getNewDir(), this.getEncoding(), this.newsource, this.excludeColumns.build());
    }

    public IDataSetWriter writer() {
        if ("xlsx".equals(this.resultType)) {
            return new XlsxDataSetWriter(this.getResultDir());
        }
        return new CsvDataSetWriterWrapper(new CsvDataSetWriter(this.getResultDir()));
    }

    private void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(this.comparisonKeySetting), "windows-31j"));
            JsonObject setting = jsonReader.read()
                    .asJsonObject();
            this.configureSetting(setting);
            this.configureCommonSetting(setting);
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new CmdLineException(parser, e);
        }
    }

    private void configureCommonSetting(JsonObject setting) {
        if(!setting.containsKey("commonSettings")){
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

    private void configureSetting(JsonObject setting) {
        setting.getJsonArray("settings")
                .stream()
                .forEach(v -> {
                    JsonObject json = v.asJsonObject();
                    if (json.containsKey("name")) {
                        String file = json.getString("name");
                        CompareSetting.Strategy strategy = CompareSetting.Strategy.BY_NAME;
                        this.addComparisonKeys(strategy, json, file);
                        this.addExcludeColumns(strategy, json, file);
                    } else if (json.containsKey("pattern")) {
                        String file = json.getString("pattern");
                        CompareSetting.Strategy strategy = CompareSetting.Strategy.PATTERN;
                        this.addComparisonKeys(strategy, json, file);
                        this.addExcludeColumns(strategy, json, file);
                    }
                });
    }

    private void addExcludeColumns(CompareSetting.Strategy strategy, JsonObject json, String file) {
        if (json.containsKey("exclude")) {
            JsonArray excludeArray = json.getJsonArray("exclude");
            List<String> columns = Lists.newArrayList();
            for (int i = 0, j = excludeArray.size(); i < j; i++) {
                columns.add(excludeArray.getString(i));
            }
            excludeColumns.add(strategy, file, columns);
        }
    }

    private void addComparisonKeys(CompareSetting.Strategy strategy, JsonObject json, String file) {
        if (json.containsKey("keys")) {
            JsonArray keyArray = json.getJsonArray("keys");
            List<String> keys = Lists.newArrayList();
            for (int i = 0, j = keyArray.size(); i < j; i++) {
                keys.add(keyArray.getString(i));
            }
            comparisonKeys.add(strategy, file, keys);
        }
    }

    private void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        if ("csv".equals(this.newsource)) {
            if (!this.newDir.exists() || !this.newDir.isDirectory()) {
                throw new CmdLineException(parser, "new is not exist directory", new IllegalArgumentException(this.newDir.toString()));
            }
        } else {
            if (!this.newDir.exists() || !this.newDir.isFile()) {
                throw new CmdLineException(parser, "new is not exist file", new IllegalArgumentException(this.newDir.toString()));
            }
        }
        if ("csv".equals(this.oldsource)) {
            if (!this.oldDir.exists() || !this.oldDir.isDirectory()) {
                throw new CmdLineException(parser, "old is not exist directory", new IllegalArgumentException(this.oldDir.toString()));
            }
        } else {
            if (!this.oldDir.exists() || !this.oldDir.isFile()) {
                throw new CmdLineException(parser, "old is not exist file", new IllegalArgumentException(this.oldDir.toString()));
            }
        }
    }
}
