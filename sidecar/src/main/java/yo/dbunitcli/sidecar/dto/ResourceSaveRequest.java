package yo.dbunitcli.sidecar.dto;

public abstract class ResourceSaveRequest<DTO> {

    private String name;

    private DTO input;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public DTO getInput() {
        return this.input;
    }

    public void setInput(final DTO input) {
        this.input = input;
    }
}
