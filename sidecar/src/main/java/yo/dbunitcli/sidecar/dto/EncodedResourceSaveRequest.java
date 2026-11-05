package yo.dbunitcli.sidecar.dto;

public abstract class EncodedResourceSaveRequest<DTO> extends ResourceSaveRequest<DTO> {

    private String encoding;

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }
}
