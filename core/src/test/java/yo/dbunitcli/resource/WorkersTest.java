package yo.dbunitcli.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WorkersTest {

    @Test
    public void defaultParallelism_is_cpu_cores_outside_worker_and_one_inside() {
        Assertions.assertEquals(Runtime.getRuntime().availableProcessors(), Workers.defaultParallelism());
        final Set<Integer> forEach = ConcurrentHashMap.newKeySet();
        Workers.forEach(2, "test-", List.of("a", "b"), (item, resources, first) -> forEach.add(Workers.defaultParallelism()));
        final Set<Integer> untilFailure = ConcurrentHashMap.newKeySet();
        Workers.forEachUntilFailure(2, "test-", Stream.of("a", "b"), item -> untilFailure.add(Workers.defaultParallelism()));
        final Set<Integer> byKey = ConcurrentHashMap.newKeySet();
        Workers.<Integer>forEachByKey(2, it -> it, it -> byKey.add(Workers.defaultParallelism())
                , submit -> IntStream.range(0, 10).boxed().forEach(submit));
        Assertions.assertEquals(Set.of(1), forEach);
        Assertions.assertEquals(Set.of(1), untilFailure);
        Assertions.assertEquals(Set.of(1), byKey);
        // 作業スレッドを抜けた呼び出し元では元に戻る
        Assertions.assertEquals(Runtime.getRuntime().availableProcessors(), Workers.defaultParallelism());
    }

    @Test
    public void forEachUntilFailure_runs_sequentially_on_caller_when_parallelism_or_items_less_than_two() {
        final Thread caller = Thread.currentThread();
        final List<String> actual = new ArrayList<>();
        Workers.forEachUntilFailure(1, "test-", Stream.of("a", "b", "c"), item -> {
            Assertions.assertSame(caller, Thread.currentThread());
            actual.add(item);
        });
        Workers.forEachUntilFailure(4, "test-", Stream.of("d"), item -> {
            Assertions.assertSame(caller, Thread.currentThread());
            actual.add(item);
        });
        Assertions.assertEquals(List.of("a", "b", "c", "d"), actual);
    }

    @Test
    public void forEachByKey_keeps_order_per_key_on_workers_with_context() {
        final Map<Integer, List<Integer>> actual = new ConcurrentHashMap<>();
        final Set<String> threads = ConcurrentHashMap.newKeySet();
        final Set<String> contexts = ConcurrentHashMap.newKeySet();
        ScopedValue.where(TestThreadContextPropagator.VALUE, "submitter")
                .run(() -> Workers.<Integer>forEachByKey(4, it -> it % 7, it -> {
                    actual.computeIfAbsent(it % 7, k -> Collections.synchronizedList(new ArrayList<>())).add(it);
                    threads.add(Thread.currentThread().getName());
                    contexts.add(TestThreadContextPropagator.VALUE.orElse("unbound"));
                }, submit -> IntStream.range(0, 1000).boxed().forEach(submit)));
        Assertions.assertEquals(7, actual.size());
        actual.forEach((key, values) -> Assertions.assertEquals(
                IntStream.range(0, 1000).filter(it -> it % 7 == key).boxed().toList(), values, "key=" + key));
        Assertions.assertTrue(threads.size() > 1, threads.toString());
        Assertions.assertEquals(Set.of("submitter"), contexts);
    }

    @Test
    public void forEachByKey_rethrows_failure_of_worker() {
        final AssertionError actual = Assertions.assertThrows(AssertionError.class
                , () -> Workers.<Integer>forEachByKey(2, it -> it, it -> {
                    if (it == 10) {
                        throw new AssertionError("failed " + it);
                    }
                }, submit -> IntStream.range(0, 10000).boxed().forEach(submit)));
        Assertions.assertEquals("failed 10", actual.getMessage());
    }

    @Test
    public void forEachByKey_rethrows_failure_of_source() {
        final Stream<Integer> items = IntStream.range(0, 100).boxed().map(it -> {
            if (it == 50) {
                throw new IllegalStateException("failed " + it);
            }
            return it;
        });
        final IllegalStateException actual = Assertions.assertThrows(IllegalStateException.class
                , () -> Workers.<Integer>forEachByKey(2, it -> it, it -> {
                }, items::forEach));
        Assertions.assertEquals("failed 50", actual.getMessage());
    }

    @Test
    public void groupByShared_groups_items_sharing_any_key() {
        final List<List<String>> actual = Workers.groupByShared(List.of("a", "b", "c", "d", "e")
                , List.of(List.of("x"), List.of("y"), List.of("z", "x"), List.of("w"), List.of("y")));
        Assertions.assertEquals(List.of(List.of("a", "c"), List.of("b", "e"), List.of("d")), actual);
    }

    @Test
    public void forEachUntilFailure_waits_running_item_and_skips_unstarted_items() {
        final List<String> started = Collections.synchronizedList(new ArrayList<>());
        final List<String> finished = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch slowStarted = new CountDownLatch(1);
        final CountDownLatch failing = new CountDownLatch(1);
        final AssertionError actual = Assertions.assertThrows(AssertionError.class
                , () -> Workers.forEachUntilFailure(2, "test-", Stream.of("slow", "fail", "x1", "x2", "x3"), item -> {
                    started.add(item);
                    try {
                        if (item.equals("slow")) {
                            slowStarted.countDown();
                            Assertions.assertTrue(failing.await(10, TimeUnit.SECONDS));
                            // 失敗の後も割り込まれずに最後まで実行される
                            Thread.sleep(100);
                        } else if (item.equals("fail")) {
                            Assertions.assertTrue(slowStarted.await(10, TimeUnit.SECONDS));
                            failing.countDown();
                            throw new AssertionError("failed " + item);
                        }
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException("interrupted " + item, e);
                    }
                    finished.add(item);
                }));
        Assertions.assertEquals("failed fail", actual.getMessage());
        Assertions.assertEquals(List.of("slow"), finished);
        Assertions.assertEquals(Set.of("slow", "fail"), Set.copyOf(started));
    }

    @Test
    public void forEachUntilFailure_processes_all_items_on_workers_with_context() {
        final Map<String, String> actual = new ConcurrentHashMap<>();
        final Set<String> threads = ConcurrentHashMap.newKeySet();
        // 2件が同時に実行中にならないとlatchが0にならない（1つのワーカーが全件を取り切らない）
        final CountDownLatch bothStarted = new CountDownLatch(2);
        ScopedValue.where(TestThreadContextPropagator.VALUE, "submitter")
                .run(() -> Workers.forEachUntilFailure(2, "test-", Stream.of("a", "b", "c", "d"), item -> {
                    actual.put(item, TestThreadContextPropagator.VALUE.orElse("unbound"));
                    threads.add(Thread.currentThread().getName());
                    bothStarted.countDown();
                    try {
                        Assertions.assertTrue(bothStarted.await(10, TimeUnit.SECONDS));
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }));
        Assertions.assertEquals(Map.of("a", "submitter", "b", "submitter", "c", "submitter", "d", "submitter"), actual);
        Assertions.assertEquals(2, threads.size());
    }
}
