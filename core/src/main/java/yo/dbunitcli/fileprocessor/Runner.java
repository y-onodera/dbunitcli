package yo.dbunitcli.fileprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.util.stream.Stream;

public interface Runner {

    Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    default void run(final Stream<File> targetFiles) {
        final PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErr) {
            @Override
            public void print(final String s) {
                Runner.LOGGER.info(s);
                super.print(s);
            }
        });
        try {
            this.runScript(targetFiles);
        } finally {
            System.setErr(sysErr);
        }
    }

    void runScript(Stream<File> targetFiles);
}
