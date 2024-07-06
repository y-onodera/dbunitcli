package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class OptionDto {

    private String name;

    private String oldName;

    private String newName;

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
}
