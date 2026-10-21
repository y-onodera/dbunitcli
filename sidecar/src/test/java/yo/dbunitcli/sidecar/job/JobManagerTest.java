package yo.dbunitcli.sidecar.job;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class JobManagerTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private JobManager jobs;

    @BeforeEach
    void setUp() {
        this.jobs = new JobManager();
    }

    @AfterEach
    void tearDown() {
        this.jobs.close();
    }

    @Test
    void testRun_実行中のログを流し結果はそのまま返す() {
        final String result = this.jobs.run("job-1", true, () -> {
            LoggerFactory.getLogger("JobManagerTest").info("hello job");
            return "result";
        });
        final List<String> logs = this.jobs.logs("job-1").collectList().block(TIMEOUT);

        Assertions.assertEquals("result", result);
        Assertions.assertEquals(1, logs.size());
        Assertions.assertTrue(logs.get(0).endsWith("INFO  JobManagerTest - hello job"), logs.get(0));
    }

    @Test
    void testRun_ジョブ外のログは流さない() {
        this.jobs.run("job-1", true, () -> "result");
        LoggerFactory.getLogger("JobManagerTest").info("outside job");

        Assertions.assertEquals(List.of(), this.jobs.logs("job-1").collectList().block(TIMEOUT));
    }

    @Test
    void testRun_例外はそのまま呼び出し元へ伝えログは終端する() {
        final IllegalStateException error = Assertions.assertThrows(IllegalStateException.class,
                () -> this.jobs.run("job-1", true, () -> {
                    throw new IllegalStateException("bad");
                }));

        Assertions.assertEquals("bad", error.getMessage());
        Assertions.assertEquals(List.of(), this.jobs.logs("job-1").collectList().block(TIMEOUT));
    }

    @Test
    void testRun_ログ不要ならログを保持せず完了時に破棄する() {
        this.jobs.run("job-1", false, () -> {
            LoggerFactory.getLogger("JobManagerTest").info("not needed");
            Assertions.assertTrue(this.jobs.find("job-1").isPresent(), "実行中は中断のため登録されること");
            return null;
        });

        Assertions.assertTrue(this.jobs.find("job-1").isEmpty());
    }

    @Test
    void testCancel_実行中のスレッドへinterruptを送る() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
        final Thread worker = Thread.ofVirtual().start(() -> this.jobs.run("job-1", true, () -> {
            started.countDown();
            try {
                Thread.sleep(Duration.ofMinutes(1));
            } catch (final InterruptedException e) {
                interrupted.countDown();
            }
            return null;
        }));
        Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));
        this.jobs.cancel("job-1");

        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "実行中のスレッドへinterruptが届くこと");
        worker.join(TIMEOUT);
    }

    @Test
    void testCancel_開始前に中断済みなら開始時に割り込む() {
        this.jobs.cancel("job-1");

        final boolean interrupted = this.jobs.run("job-1", true, () -> Thread.currentThread().isInterrupted());

        Assertions.assertTrue(interrupted);
        Assertions.assertFalse(Thread.currentThread().isInterrupted(), "完了後は割り込み状態を持ち越さないこと");
    }
}
