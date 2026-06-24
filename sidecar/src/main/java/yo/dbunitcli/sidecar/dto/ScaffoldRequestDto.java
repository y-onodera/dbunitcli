package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public class ScaffoldRequestDto {

    private String commandType;

    private List<String> generateTargets;

    private String name;

    private Map<String, String> input;

    private Map<String, String> scaffoldInput;

    public String getCommandType() {
        return this.commandType;
    }

    public void setCommandType(final String commandType) {
        this.commandType = commandType;
    }

    public List<String> getGenerateTargets() {
        return this.generateTargets;
    }

    public void setGenerateTargets(final List<String> generateTargets) {
        this.generateTargets = generateTargets;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, String> getInput() {
        return this.input;
    }

    public void setInput(final Map<String, String> input) {
        this.input = input;
    }

    public Map<String, String> getScaffoldInput() {
        return this.scaffoldInput;
    }

    public void setScaffoldInput(final Map<String, String> scaffoldInput) {
        this.scaffoldInput = scaffoldInput;
    }
}
