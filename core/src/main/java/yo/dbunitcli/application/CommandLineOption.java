package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.util.Optional;

public interface CommandLineOption<T extends CommandDto> extends Option {

    ArgumentFilter ARGUMENT_FILTER = new DefaultArgumentFilter("-P");
    ArgumentFunction ARGUMENT_FUNCTION = new DefaultArgumentFunction();

    default String[] toArgs(final boolean containDefaultValue) {
        return this.toParameters().toList(containDefaultValue).toArray(new String[0]);
    }

    T toDto();

    /**
     * 連動・排斥の検証ルールを返す。ルールは{@link CommandLine.ArgGroup}と{@link CommandLine.Option}で宣言したクラスの新しいインスタンスで、
     * オプション名は展開後の名前（-setting、-new.src等のprefix付き）で書く。ルールに無いオプションは検証対象外
     */
    default Optional<?> validationRule() {
        return Optional.empty();
    }

    /**
     * 展開済みの引数（値が空のものは未指定扱い）を検証ルールでパースし、違反があれば{@link OptionValidationException}を投げる
     */
    default void validate() {
        this.validationRule().ifPresent(rule -> {
            try {
                new CommandLine(rule).setUnmatchedArgumentsAllowed(true).parseArgs(this.toArgs(false));
            } catch (final CommandLine.ParameterException ex) {
                throw new OptionValidationException(ex.getMessage(), ex);
            }
        });
    }

    default ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    default ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter());
    }

    Parameter parameter();

}
