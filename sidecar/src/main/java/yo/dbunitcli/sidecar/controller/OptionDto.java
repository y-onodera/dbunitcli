package yo.dbunitcli.sidecar.controller;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.application.CommandDto;

@Serdeable
public class OptionDto<T extends CommandDto> {

    private T value;

    private String name;

    public T getValue() {
        return this.value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
