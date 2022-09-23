package yo.dbunitcli.fileprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;

public interface Runner {

    Logger LOGGER = LogManager.getLogger();

    default void run(Collection<File> targetFiles) throws DataSetException {
        PrintStream sysErr = System.err;
        System.setErr(new PrintStream(sysErr) {
            @Override
            public void print(String s) {
                LOGGER.info(s);
                super.print(s);
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
