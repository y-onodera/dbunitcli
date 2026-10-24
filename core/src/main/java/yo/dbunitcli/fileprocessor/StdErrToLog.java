package yo.dbunitcli.fileprocessor;

import java.io.PrintStream;

/**
 * Runner実行中のSystem.errをログへも転送する。Parameterizeの並列実行などでRunnerが同時に動いても、
 * 差し替えは最初の1回だけにし、最後のRunnerが終わったときに元へ戻す（戻す順が前後して差し替えたままにならない）
 */
final class StdErrToLog {

    private static int running;

    private static PrintStream original;

    private StdErrToLog() {
    }

    static synchronized void begin() {
        if (StdErrToLog.running++ == 0) {
            StdErrToLog.original = System.err;
            System.setErr(new PrintStream(StdErrToLog.original) {
                @Override
                public void print(final String s) {
                    Runner.LOGGER.info(s);
                    super.print(s);
                }
            });
        }
    }

    static synchronized void end() {
        if (--StdErrToLog.running == 0) {
            System.setErr(StdErrToLog.original);
            StdErrToLog.original = null;
        }
    }
}
