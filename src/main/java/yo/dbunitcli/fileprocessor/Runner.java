package yo.dbunitcli.fileprocessor;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;

public interface Runner {

    Logger logger = LoggerFactory.getLogger(Runner.class);

    default void run(Collection<File> targetFiles) throws DataSetException {
        PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErr) {
            @Override
            public void print(String s) {
                logger.info(s);
                super.print(s);
            }

            @Override
            public void println(String x) {
                logger.info(x);
                super.println(x);
            }
        });
        try {
            runScript(targetFiles);
        } finally {
            System.setErr(sysErr);
        }
    }

    void runScript(Collection<File> targetFiles) throws DataSetException;
}
