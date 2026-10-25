package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.application.OptionValidationException;

import java.util.List;

/**
 * オプション検証エラーの応答。GUIはviolationsのoptions（入力欄のname）に対応する欄へmessageを表示する
 */
@Serdeable
public record OptionValidationErrorDto(String message, List<Violation> violations) {

    public static OptionValidationErrorDto of(final OptionValidationException ex) {
        return new OptionValidationErrorDto(ex.getMessage(), ex.getViolations().stream()
                                                               .map(it -> new Violation(it.options(), it.message()))
                                                               .toList());
    }

    @Serdeable
    public record Violation(List<String> options, String message) {
    }
}
