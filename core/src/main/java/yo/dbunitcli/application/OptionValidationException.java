package yo.dbunitcli.application;

/**
 * 展開済みのオプションが検証（必須・排斥・ファイル存在）に違反していることを表す。
 * 差分検出などのコマンド失敗（{@link Command.CommandFailException}）とは区別し、ignoreFailの対象にしない
 */
public class OptionValidationException extends RuntimeException {

    public OptionValidationException(final String message) {
        super(message);
    }
}
