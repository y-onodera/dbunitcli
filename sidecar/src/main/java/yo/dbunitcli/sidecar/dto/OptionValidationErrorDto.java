package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.application.Option;

import java.util.List;

/**
 * オプション検証エラーの応答。GUIはviolationsのoptions（入力欄のname）に対応する欄へmessageを表示する
 */
@Serdeable
public record OptionValidationErrorDto(String message, List<Option.Violation> violations) {
}
