package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableJoinDto {
    private String left;
    private String right;
    private List<String> column;
    private String on;
    private String filePath;
    private String leftFilePath;
    private String rightFilePath;

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public String getLeftFilePath() {
        return this.leftFilePath;
    }

    public void setLeftFilePath(final String leftFilePath) {
        this.leftFilePath = leftFilePath;
    }

    public String getRightFilePath() {
        return this.rightFilePath;
    }

    public void setRightFilePath(final String rightFilePath) {
        this.rightFilePath = rightFilePath;
    }

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
