package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

/**
 * separate/join後の出力テーブルを絞る設定。nameかpatternのいずれかを指定する(filePathは非対応)。
 */
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputFilterDto {
    private NameDto name;
    private PatternDto pattern;

    public NameDto getName() {
        return this.name;
    }

    public void setName(final NameDto name) {
        this.name = name;
    }

    public PatternDto getPattern() {
        return this.pattern;
    }

    public void setPattern(final PatternDto pattern) {
        this.pattern = pattern;
    }
}
