package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class TypedRefreshRequestDto {

    private String commandType;

    private Map<String, String> input;

    public String getCommandType() {
        return this.commandType;
    }

    public void setCommandType(final String commandType) {
        this.commandType = commandType;
    }

    public Map<String, String> getInput() {
        return this.input;
    }

    public void setInput(final Map<String, String> input) {
        this.input = input;
    }
}
