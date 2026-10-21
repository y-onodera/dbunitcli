package yo.dbunitcli.sidecar.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class JobContextTest {

    @Test
    void testRun_実行中だけ現在のジョブとして参照でき結果はそのまま返す() {
        final List<String> logs = new ArrayList<>();
        final JobContext job = new JobContext(logs::add);

        final String result = job.run(() -> {
            JobContext.current().orElseThrow().log("hello job");
            return "result";
        });

        Assertions.assertEquals("result", result);
        Assertions.assertEquals(List.of("hello job"), logs);
        Assertions.assertTrue(JobContext.current().isEmpty(), "完了後は現在のジョブとして残らないこと");
    }

    @Test
    void testRun_例外はそのまま呼び出し元へ伝える() {
        final JobContext job = new JobContext(line -> {
        });

        final IllegalStateException error = Assertions.assertThrows(IllegalStateException.class,
                () -> job.run(() -> {
                    throw new IllegalStateException("bad");
                }));

        Assertions.assertEquals("bad", error.getMessage());
        Assertions.assertTrue(JobContext.current().isEmpty());
    }

    @Test
    void testCancel_実行中のスレッドへinterruptを送る() throws Exception {
        final JobContext job = new JobContext(line -> {
        });
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);
        final Thread worker = Thread.ofVirtual().start(() -> job.run(() -> {
            started.countDown();
            try {
                Thread.sleep(Duration.ofMinutes(1));
            } catch (final InterruptedException e) {
                interrupted.countDown();
            }
            return null;
        }));
        Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));

        job.cancel();

        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "実行中のスレッドへinterruptが届くこと");
        worker.join(Duration.ofSeconds(10));
    }

    @Test
    void testCancel_開始前に中断済みなら開始時に割り込む() {
        final JobContext job = new JobContext(line -> {
        });
        job.cancel();

        final boolean interrupted = job.run(() -> Thread.currentThread().isInterrupted());

        Assertions.assertTrue(interrupted);
        Assertions.assertFalse(Thread.currentThread().isInterrupted(), "完了後は割り込み状態を持ち越さないこと");
    }
}
