package example.y.onodera.dbunitcli;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    private String encoding = "windows-31j";

    @Option(name = "-old", usage = "directory old files at", required = true)
    private File oldDir;

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

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
        if (!this.newDir.exists() || !this.newDir.isDirectory()) {
            throw new CmdLineException(parser, "newDir is not exist directory", new IllegalArgumentException("newDir is not exist directory"));
        }
        if (!this.oldDir.exists() || !this.oldDir.isDirectory()) {
            throw new CmdLineException(parser, "oldDir is not exist directory", new IllegalArgumentException("oldDir is not exist directory"));
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
}
