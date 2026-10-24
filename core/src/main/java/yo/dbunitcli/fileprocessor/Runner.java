package yo.dbunitcli.fileprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ParallelTableMapping;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Stream;

public interface Runner {

    Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    /**
     * @param parallelism 並列数。2以上ならファイルを作業スレッドで並列に実行する（実行順は不定）。1以下は逐次
     */
    default void run(final Stream<File> targetFiles, final int parallelism) {
        final PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErr) {
            @Override
            public void print(final String s) {
                Runner.LOGGER.info(s);
                super.print(s);
            }
        });
        try {
            if (parallelism < 2) {
                targetFiles.forEach(this::runScript);
                return;
            }
            final List<File> files = targetFiles.toList();
            if (files.size() < 2) {
                files.forEach(this::runScript);
                return;
            }
            ParallelTableMapping.forEach(parallelism, "run-", files, (target, resources, first) -> this.runScript(target));
        } finally {
            System.setErr(sysErr);
        }
    }

    void runScript(File target);
}
