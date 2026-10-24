package yo.dbunitcli.application.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ParallelTableMapping;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Parameterize implements Command<ParameterizeDto, ParameterizeOption> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parameterize.class);

    public static void main(final String[] args) {
        new Parameterize().exec(args);
    }

    /**
     * パラメータごとにコマンドを実行する。-parallelが2以上なら作業スレッドで並列に実行する（実行順は不定）。
     * ignoreFail=falseで失敗したら未着手のコマンドは始めず、並列時は実行中のコマンドが終わるのを待ってから失敗させる
     */
    @Override
    public void exec(final ParameterizeOption options) {
        final AtomicInteger failCount = new AtomicInteger();
        final Consumer<Parameter> action = it -> {
            try {
                options.createCommand(it).exec(options.createArgs(it), it);
            } catch (final Command.CommandFailException fail) {
                if (!options.ignoreFail()) {
                    throw fail;
                }
                Parameterize.LOGGER.info(fail.getMessage());
                failCount.incrementAndGet();
            }
        };
        try (final Stream<Parameter> params = options.loadParams()) {
            if (options.parallelism() < 2) {
                params.forEach(action);
            } else {
                final List<Parameter> items = params.toList();
                if (items.size() < 2) {
                    items.forEach(action);
                } else {
                    ParallelTableMapping.forEachUntilFailure(options.parallelism(), "parameterize-", items, action);
                }
            }
        }
        if (failCount.get() > 0) {
            Parameterize.LOGGER.info("total fail count:{}", failCount.get());
            throw new Command.CommandFailException("execute has fail count");
        }
    }

    @Override
    public ParameterizeDto createDto(final String[] args) {
        return ParameterizeOption.toDto(args);
    }

    @Override
    public ParameterizeOption parseOption(final String resultFile, final ParameterizeDto dto, final Parameter param) {
        return new ParameterizeOption(dto, param);
    }

}
