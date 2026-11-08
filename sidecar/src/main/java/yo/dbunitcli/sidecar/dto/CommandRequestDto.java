package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;

@Serdeable
public class CommandRequestDto {

    private String name;

    private String oldName;

    private String newName;

    private Map<String, String> input;

    private List<String> parameterizeKeys;

    private String paramType;

    private String paramEncoding;

    private List<String> deleteFiles = List.of();

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getOldName() {
        return this.oldName;
    }

    public void setOldName(final String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return this.newName;
    }

    public void setNewName(final String newName) {
        this.newName = newName;
    }

    public Map<String, String> getInput() {
        return this.input;
    }

    public void setInput(final Map<String, String> input) {
        this.input = input;
    }

    public List<String> getParameterizeKeys() {
        return this.parameterizeKeys;
    }

    public void setParameterizeKeys(final List<String> parameterizeKeys) {
        this.parameterizeKeys = parameterizeKeys;
    }

    public String getParamType() {
        return this.paramType;
    }

    public void setParamType(final String paramType) {
        this.paramType = paramType;
    }

    public String getParamEncoding() {
        return this.paramEncoding;
    }

    public void setParamEncoding(final String paramEncoding) {
        this.paramEncoding = paramEncoding;
    }

    public List<String> getDeleteFiles() {
        return this.deleteFiles;
    }

    public void setDeleteFiles(final List<String> deleteFiles) {
        this.deleteFiles = deleteFiles;
    }
}
