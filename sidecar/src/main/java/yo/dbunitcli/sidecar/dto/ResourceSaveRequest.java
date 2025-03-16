package yo.dbunitcli.sidecar.dto;

public interface ResourceSaveRequest<DTO> {

    String getName();

    DTO getInput();

}
