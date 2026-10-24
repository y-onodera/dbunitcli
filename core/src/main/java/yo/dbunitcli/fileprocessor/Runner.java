package yo.dbunitcli.fileprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.Workers;

import java.io.File;
import java.util.stream.Stream;

public interface Runner {

    Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    /**
     * @param parallelism 並列数。2以上ならファイルを作業スレッドで並列に実行する（実行順は不定）。1以下は逐次。
     *                    並列時に失敗したら未着手のファイルは始めず、実行中のファイルが終わるのを待ってから失敗させる
     */
    default void run(final Stream<File> targetFiles, final int parallelism) {
        StdErrToLog.begin();
        try {
            Workers.forEachUntilFailure(parallelism, "run-", targetFiles, this::runScript);
        } finally {
            StdErrToLog.end();
        }
    }

    void runScript(File target);
}
