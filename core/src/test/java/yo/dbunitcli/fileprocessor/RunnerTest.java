package yo.dbunitcli.fileprocessor;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
}
