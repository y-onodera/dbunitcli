package yo.dbunitcli.application;

import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

public class CommandDto {

    @CommandLine.Option(names = "-P")
    private Map<String, String> inputParam = new HashMap<>();

    public Map<String, String> getInputParam() {
        return this.inputParam;
    }

    public void setInputParam(final Map<String, String> inputParam) {
        this.inputParam = inputParam;
    }

}
