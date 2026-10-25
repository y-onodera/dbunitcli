package yo.dbunitcli.dataset.converter;

import java.util.List;

public record FixedColumnDef(String name, int length, String align, String pad) {

    public static final List<String> ALIGNS = List.of("left", "right");

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
