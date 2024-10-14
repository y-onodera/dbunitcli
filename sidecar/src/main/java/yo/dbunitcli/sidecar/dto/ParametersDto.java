package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParametersDto {

    private List<String> convert = new ArrayList<>();

    private List<String> compare = new ArrayList<>();

    private List<String> generate = new ArrayList<>();

    private List<String> run = new ArrayList<>();

    private List<String> parameterize = new ArrayList<>();

    public List<String> getConvert() {
        return this.convert;
    }

    public void setConvert(final List<String> convert) {
        this.convert = convert;
    }

    public List<String> getCompare() {
        return this.compare;
    }

    public void setCompare(final List<String> compare) {
        this.compare = compare;
    }

    public List<String> getGenerate() {
        return this.generate;
    }

    public void setGenerate(final List<String> generate) {
        this.generate = generate;
    }

    public List<String> getRun() {
        return this.run;
    }

    public void setRun(final List<String> run) {
        this.run = run;
    }

    public List<String> getParameterize() {
        return this.parameterize;
    }

    public void setParameterize(final List<String> parameterize) {
        this.parameterize = parameterize;
    }

}
