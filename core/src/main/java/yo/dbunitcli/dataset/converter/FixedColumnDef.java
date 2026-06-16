package yo.dbunitcli.dataset.converter;

public record FixedColumnDef(String name, int length, boolean leftAlign, String pad) {

    public FixedColumnDef {
        if (pad == null || pad.isEmpty()) {
            pad = " ";
        }
    }

    public String align() {
        return this.leftAlign ? "left" : "right";
    }
}
