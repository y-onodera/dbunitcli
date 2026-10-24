package yo.dbunitcli.resource;

import java.util.List;
import java.util.ServiceLoader;

/**
 * 別スレッドへ処理を渡すときに、投入元スレッドの文脈を引き継ぐ。
 * FileResourcesのContextは常に引き継ぎ、それ以外（sidecarのログ振り分け先など）は登録された{@link ThreadContextPropagator}で引き継ぐ
 */
public final class ThreadContexts {

    private static final List<ThreadContextPropagator> PROPAGATORS = ServiceLoader.load(ThreadContextPropagator.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .toList();

    private ThreadContexts() {
    }

    /**
     * 投入元スレッドで呼び、文脈を引き継いだRunnableを返す
     */
    public static Runnable propagate(final Runnable task) {
        Runnable result = FileResources.propagateContext(task);
        for (final ThreadContextPropagator propagator : ThreadContexts.PROPAGATORS) {
            result = propagator.wrap(result);
        }
        return result;
    }
}
