package yo.dbunitcli.fileprocessor;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RunnerTest {

    private static Stream<File> files(final String... names) {
        return Stream.of(names).map(File::new);
    }

    @Test
    void sequentialKeepsOrderOnCallerThread() {
        final List<String> executed = new CopyOnWriteArrayList<>();
        final Thread caller = Thread.currentThread();
        final Runner target = file -> {
            assertSame(caller, Thread.currentThread());
            executed.add(file.getName());
        };
        target.run(RunnerTest.files("a", "b", "c"), 1);
        assertEquals(List.of("a", "b", "c"), executed);
    }

    @Test
    void parallelRunsConcurrently() {
        // 2ファイルが同時に実行中にならないとlatchが0にならない
        final CountDownLatch bothStarted = new CountDownLatch(2);
        final Set<String> threads = ConcurrentHashMap.newKeySet();
        final Set<String> executed = ConcurrentHashMap.newKeySet();
        final Runner target = file -> {
            threads.add(Thread.currentThread().getName());
            bothStarted.countDown();
            try {
                assertTrue(bothStarted.await(10, TimeUnit.SECONDS));
            } catch (final InterruptedException e) {
                throw new AssertionError(e);
            }
            executed.add(file.getName());
        };
        target.run(RunnerTest.files("a", "b"), 2);
        assertEquals(Set.of("a", "b"), executed);
        assertEquals(2, threads.size());
        assertTrue(threads.stream().allMatch(it -> it.startsWith("run-")));
    }

    @Test
    void parallelRethrowsFailure() {
        final Runner target = file -> {
            if (file.getName().equals("b")) {
                throw new AssertionError("fail " + file.getName());
            }
        };
        final AssertionError error = assertThrows(AssertionError.class, () -> target.run(RunnerTest.files("a", "b", "c"), 2));
        assertEquals("fail b", error.getMessage());
    }

    @Test
    void parallelWithSingleFileRunsOnCallerThread() {
        final Thread caller = Thread.currentThread();
        final List<String> executed = new CopyOnWriteArrayList<>();
        final Runner target = file -> {
            assertSame(caller, Thread.currentThread());
            executed.add(file.getName());
        };
        target.run(RunnerTest.files("a"), 4);
        assertEquals(List.of("a"), executed);
    }

    @Test
    void parallelFailureWaitsRunningScriptAndSkipsUnstarted() {
        final List<String> started = new CopyOnWriteArrayList<>();
        final List<String> finished = new CopyOnWriteArrayList<>();
        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch failing = new CountDownLatch(1);
        final Runner target = file -> {
            started.add(file.getName());
            if (file.getName().equals("slow")) {
                slowStarted.countDown();
                RunnerTest.await(failing);
                try {
                    // 失敗の後も割り込まれずに最後まで実行される
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    throw new IllegalStateException("interrupted", e);
                }
            } else if (file.getName().equals("fail")) {
                RunnerTest.await(slowStarted);
                failing.countDown();
                throw new AssertionError("fail");
            }
            finished.add(file.getName());
        };
        final AssertionError error = assertThrows(AssertionError.class
                , () -> target.run(RunnerTest.files("slow", "fail", "x1", "x2"), 2));
        assertEquals("fail", error.getMessage());
        assertEquals(List.of("slow"), finished);
        assertEquals(Set.of("slow", "fail"), Set.copyOf(started));
    }

    @Test
    void stdErrIsRestoredWhenRunnersOverlap() throws Exception {
        // 2つのRunnerを重ねて実行し、先に始めた方が先に終わっても後の方が終わるまでは差し替えたまま、最後に元へ戻る
        final PrintStream original = System.err;
        final CountDownLatch firstStarted = new CountDownLatch(1);
        final CountDownLatch secondStarted = new CountDownLatch(1);
        final CountDownLatch firstFinished = new CountDownLatch(1);
        final AtomicReference<PrintStream> afterFirst = new AtomicReference<>();
        final Thread first = new Thread(() -> ((Runner) file -> {
            firstStarted.countDown();
            RunnerTest.await(secondStarted);
        }).run(RunnerTest.files("a"), 1));
        final Thread second = new Thread(() -> ((Runner) file -> {
            RunnerTest.await(firstStarted);
            secondStarted.countDown();
            RunnerTest.await(firstFinished);
            afterFirst.set(System.err);
        }).run(RunnerTest.files("b"), 1));
        first.start();
        second.start();
        first.join(10000);
        firstFinished.countDown();
        second.join(10000);
        assertNotSame(original, afterFirst.get());
        assertSame(original, System.err);
    }

    private static void await(final CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
