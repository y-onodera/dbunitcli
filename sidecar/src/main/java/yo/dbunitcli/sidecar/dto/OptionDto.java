package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Map;

@Serdeable
public class OptionDto {

    private String name;

    private String oldName;

    private String newName;

    private Map<String, String> input;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOldName() {
        return this.oldName;
    }

    public void setOldName(final String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return this.newName;
    }

    public void setNewName(final String newName) {
        this.newName = newName;
    }

    public Map<String, String> getInput() {
        return this.input;
    }

    public void setInput(final Map<String, String> input) {
        this.input = input;
    }
}
