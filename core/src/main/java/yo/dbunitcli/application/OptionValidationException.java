package yo.dbunitcli.application;

import java.util.List;

/**
 * 展開済みのオプションが検証（必須・排斥・ファイル存在）に違反していることを表す。
 * 差分検出などのコマンド失敗（{@link Command.CommandFailException}）とは区別し、ignoreFailの対象にしない
 */
public class OptionValidationException extends RuntimeException {

    private final List<Option.Violation> violations;

    public OptionValidationException(final List<Option.Violation> violations) {
        // sidecarの応答にも載るため、OSに依存しない改行でつなぐ
        super(String.join("\n", violations.stream().map(Option.Violation::message).toList()));
        this.violations = List.copyOf(violations);
    }

    public List<Option.Violation> getViolations() {
        return this.violations;
    }
}
