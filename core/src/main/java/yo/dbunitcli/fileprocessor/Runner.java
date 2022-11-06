package yo.dbunitcli.fileprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;

public interface Runner {

    Logger LOGGER = LogManager.getLogger();

    default void run(final Collection<File> targetFiles) {
        final PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErr) {
            @Override
            public void print(final String s) {
                LOGGER.info(s);
                super.print(s);
            }
        });
        try {
            this.runScript(targetFiles);
        } finally {
            System.setErr(sysErr);
        }
    }

    void runScript(Collection<File> targetFiles);
}
