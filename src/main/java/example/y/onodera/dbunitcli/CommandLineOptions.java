package example.y.onodera.dbunitcli;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import example.y.onodera.dbunitcli.dataset.ComparableCSVDataSet;
import example.y.onodera.dbunitcli.dataset.ComparableDataSet;
import example.y.onodera.dbunitcli.dataset.ComparableXlsDataSet;
import example.y.onodera.dbunitcli.dataset.ComparableXlsxDataSet;
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

    @Option(name = "-oldsource", usage = "csv | xlx | xlsx : default csv")
    private String oldsource = "csv";

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

    @Option(name = "-newsource", usage = "csv | xlx | xlsx : default csv")
    private String newsource = "csv";

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File("").getAbsoluteFile();

    @Option(name = "-setting", usage = "file define comparison settings", required = true)
    private File comparisonKeySetting;

    @Option(name = "-expect", usage = "expected diff")
    private File expected;

    private Map<String, List<String>> comparisonKeys = Maps.newHashMap();

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

    private void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(this.comparisonKeySetting), "windows-31j"));
            jsonReader.read()
                    .asJsonObject()
                    .getJsonArray("settings")
                    .stream()
                    .forEach(v -> {
                        JsonObject json = v.asJsonObject();
                        final String file = json.getString("file");
                        JsonArray array = json.getJsonArray("keys");
                        List<String> keys = Lists.newArrayList();
                        for (int i = 0, j = array.size(); i < j; i++) {
                            keys.add(array.getString(i));
                        }
                        comparisonKeys.put(file, keys);
                    });
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            throw new CmdLineException(parser, e);
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
        }else{
            if (!this.oldDir.exists() || !this.oldDir.isFile()) {
                throw new CmdLineException(parser, "old is not exist file", new IllegalArgumentException(this.oldDir.toString()));
            }
        }
    }

    public String getEncoding() {
        return encoding;
    }

    public File getOldDir() {
        return oldDir;
    }

    public File getNewDir() {
        return newDir;
    }

    public File getResultDir() {
        return resultDir;
    }

    public Map<String, List<String>> getComparisonKeys() {
        return comparisonKeys;
    }

    public File getExpected() {
        return expected;
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        switch (this.oldsource) {
            case "xlsx":
                return new ComparableXlsxDataSet(this.oldDir);
            case "xls":
                return new ComparableXlsDataSet(this.oldDir);
            default:
                return new ComparableCSVDataSet(this.getOldDir(), this.getEncoding());
        }
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        switch (this.newsource) {
            case "xlsx":
                return new ComparableXlsxDataSet(this.newDir);
            case "xls":
                return new ComparableXlsDataSet(this.newDir);
            default:
                return new ComparableCSVDataSet(this.getNewDir(), this.getEncoding());
        }
    }
}
