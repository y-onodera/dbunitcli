package yo.dbunitcli.application;

/**
 * 展開済みのオプションが検証ルール（連動・排斥）に違反していることを表す。
 * 差分検出などのコマンド失敗（{@link Command.CommandFailException}）とは区別し、ignoreFailの対象にしない
 */
public class OptionValidationException extends RuntimeException {

    public OptionValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
