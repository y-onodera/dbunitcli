package yo.dbunitcli.sidecar.dto;

public abstract class ResourceSaveRequest<DTO> extends ResourceLoadRequestDto {

    private DTO input;

    public DTO getInput() {
        return this.input;
    }

    public void setInput(final DTO input) {
        this.input = input;
    }
}
