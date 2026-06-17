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

    // ST4テンプレートはJavaBean規約でプロパティアクセスするため必要
    public String getName() { return this.name; }
    public int getLength() { return this.length; }
    public String getAlign() { return this.align(); }
    public String getPad() { return this.pad; }
}
