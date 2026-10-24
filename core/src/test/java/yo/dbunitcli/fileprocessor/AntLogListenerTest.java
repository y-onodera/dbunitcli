package yo.dbunitcli.fileprocessor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Antのメッセージ（ビルドファイルのタスク・SQLExec）がSystem.errではなくログへ出る
 */
class AntLogListenerTest {

    @TempDir
    Path tempDir;

    private final Logger logger = (Logger) LoggerFactory.getLogger(AntLogListener.class);

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private Level level;

    @BeforeEach
    void setUp() {
        this.level = this.logger.getLevel();
        this.logger.setLevel(Level.DEBUG);
        this.appender.start();
        this.logger.addAppender(this.appender);
    }

    @AfterEach
    void tearDown() {
        this.logger.detachAppender(this.appender);
        this.logger.setLevel(this.level);
    }

    @Test
    void antTaskMessagesAreLoggedByPriority() throws IOException {
        final File build = this.tempDir.resolve("build.xml").toFile();
        Files.writeString(build.toPath(), """
                <project default="t">
                    <target name="t">
                        <echo message="hello" level="info"/>
                        <echo message="careful" level="warning"/>
                        <echo message="detail" level="verbose"/>
                    </target>
                </project>
                """, StandardCharsets.UTF_8);
        new AntRunner(this.tempDir.toFile(), "", Parameter.NONE).runScript(build);
        final List<String> actual = this.messages();
        Assertions.assertTrue(actual.contains("INFO [echo] hello"), actual.toString());
        Assertions.assertTrue(actual.contains("WARN [echo] careful"), actual.toString());
        Assertions.assertTrue(actual.contains("DEBUG [echo] detail"), actual.toString());
    }

    @Test
    void sqlExecMessagesAreLoggedNotWrittenToStdErr() throws IOException {
        final File sql = this.tempDir.resolve("create.sql").toFile();
        Files.writeString(sql.toPath(), "create table T (ID int);", StandardCharsets.UTF_8);
        final Properties jdbc = new Properties();
        jdbc.put("url", "jdbc:h2:mem:antLogListener;DB_CLOSE_DELAY=-1");
        jdbc.put("user", "sa");
        jdbc.put("pass", "");
        final SqlRunner target = new SqlRunner(new DatabaseConnectionLoader(jdbc), Parameter.NONE, TemplateRender.builder().build());
        final PrintStream original = System.err;
        final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stdErr, true, StandardCharsets.UTF_8));
        try {
            target.runScript(sql);
        } finally {
            System.setErr(original);
        }
        Assertions.assertEquals("", stdErr.toString(StandardCharsets.UTF_8));
        Assertions.assertTrue(this.messages().contains("INFO [sql] 1 of 1 SQL statements executed successfully"), this.messages().toString());
    }

    private List<String> messages() {
        return this.appender.list.stream().map(it -> it.getLevel() + " " + it.getFormattedMessage()).toList();
    }
}
