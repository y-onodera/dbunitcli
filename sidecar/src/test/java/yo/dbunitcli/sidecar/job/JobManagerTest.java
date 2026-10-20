package yo.dbunitcli.sidecar.job;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.sidecar.controller.ApplicationException;
import yo.dbunitcli.sidecar.dto.JobEventDto;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    void testStart_仮想スレッドで実行しログと結果をイベントで返す() throws Exception {
        final AtomicBoolean virtual = new AtomicBoolean();
        final Job job = this.start(() -> {
            virtual.set(Thread.currentThread().isVirtual());
            LoggerFactory.getLogger("JobManagerTest").info("hello job");
            return "result";
        });
        final List<JobEventDto> events = job.events().collectList().block(TIMEOUT);

        Assertions.assertTrue(virtual.get(), "仮想スレッドで実行されること");
        Assertions.assertEquals(2, events.size());
        Assertions.assertEquals(JobEventDto.TYPE_LOG, events.get(0).getType());
        Assertions.assertTrue(events.get(0).getMessage().endsWith("INFO  JobManagerTest - hello job"), events.get(0).getMessage());
        final JobEventDto done = events.get(1);
        Assertions.assertEquals(JobEventDto.TYPE_DONE, done.getType());
        Assertions.assertEquals("SUCCEEDED", done.getStatus());
        Assertions.assertEquals(200, done.getHttpStatus());
        Assertions.assertEquals("result", done.getResult());
    }

    @Test
    void testStart_ジョブ外のスレッドのログは転送しない() throws Exception {
        final CountDownLatch logged = new CountDownLatch(1);
        final Job job = this.start(() -> {
            Assertions.assertTrue(logged.await(10, TimeUnit.SECONDS));
            return "result";
        });
        LoggerFactory.getLogger("JobManagerTest").info("outside job");
        logged.countDown();
        final List<JobEventDto> events = job.events().collectList().block(TIMEOUT);

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(JobEventDto.TYPE_DONE, events.get(0).getType());
    }

    @Test
    void testStart_ApplicationExceptionは400で失敗() throws Exception {
        final Job job = this.start(() -> {
            throw new ApplicationException(new IllegalArgumentException("bad input"));
        });
        final JobEventDto done = job.events().blockLast(TIMEOUT);

        Assertions.assertEquals("FAILED", done.getStatus());
        Assertions.assertEquals(400, done.getHttpStatus());
        Assertions.assertEquals("Fix Input Parameter", done.getReason());
        Assertions.assertEquals("Execution failed. cause: java.lang.IllegalArgumentException: bad input", done.getMessage());
    }

    @Test
    void testStart_IOExceptionは500で失敗() throws Exception {
        final Job job = this.start(() -> {
            throw new IOException("disk error");
        });
        final JobEventDto done = job.events().blockLast(TIMEOUT);

        Assertions.assertEquals("FAILED", done.getStatus());
        Assertions.assertEquals(500, done.getHttpStatus());
        Assertions.assertEquals("File operation failed. cause: disk error", done.getMessage());
    }

    @Test
    void testCancel_実行中のジョブへinterruptを送り中断扱いにする() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
        final Job job = this.start(() -> {
            started.countDown();
            try {
                Thread.sleep(Duration.ofMinutes(1));
            } catch (final InterruptedException e) {
                interrupted.countDown();
            }
            return "never";
        });
        Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));
        this.jobs.cancel(job.getId());
        final JobEventDto done = job.events().blockLast(TIMEOUT);

        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "仮想スレッドへinterruptが届くこと");
        Assertions.assertEquals("CANCELLED", done.getStatus());
        Assertions.assertNull(done.getResult());
        Assertions.assertEquals(Job.Status.CANCELLED, job.getStatus());
    }

    private Job start(final java.util.concurrent.Callable<String> task) throws IOException {
        final String json = this.jobs.start(task);
        final String id = json.replaceAll(".*\"jobId\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        return this.jobs.find(id).orElseThrow();
    }
}
