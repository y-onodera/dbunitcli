package yo.dbunitcli.dataset.converter;

public record FixedColumnDef(String name, int length, String align, String pad) {

    // ST4テンプレートはJavaBean規約でプロパティアクセスするため必要
    public String getName() {
        return this.name;
    }

    public int getLength() {
        return this.length;
    }

    public String getAlign() {
        return this.align;
    }

    public String getPad() {
        return this.pad;
    }

    public boolean leftAlign() {
        return "left".equals(this.align);
    }
}
