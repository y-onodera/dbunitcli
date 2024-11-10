package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class TableJoinDto {
    private String left;
    private String right;
    private List<String> column;
    private String on;

    public String getLeft() {
        return this.left;
    }

    public void setLeft(final String left) {
        this.left = left;
    }

    public String getRight() {
        return this.right;
    }

    public void setRight(final String right) {
        this.right = right;
    }

    public List<String> getColumn() {
        return this.column;
    }

    public void setColumn(final List<String> column) {
        this.column = column;
    }

    public String getOn() {
        return this.on;
    }

    public void setOn(final String on) {
        this.on = on;
    }
}
