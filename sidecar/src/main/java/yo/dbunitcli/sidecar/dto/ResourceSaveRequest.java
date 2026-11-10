package yo.dbunitcli.sidecar.dto;

public abstract class ResourceSaveRequest<DTO> extends ResourceLoadRequestDto {

    private DTO input;

    // trueなら読込と同じ探索で見つかったファイルへ上書きする。falseならnameへ新規保存（既存なら上書き）
    private boolean overwrite;

    public boolean isOverwrite() {
        return this.overwrite;
    }

    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    public DTO getInput() {
        return this.input;
    }

    public void setInput(final DTO input) {
        this.input = input;
    }
}
