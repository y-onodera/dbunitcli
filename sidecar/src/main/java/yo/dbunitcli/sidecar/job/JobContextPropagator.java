package yo.dbunitcli.sidecar.job;

import yo.dbunitcli.resource.ThreadContextPropagator;

/**
 * core内部で別スレッドへ渡す処理に、投入元スレッドのジョブ（ログの振り分け先）を引き継ぐ。
 * META-INF/servicesで登録し、ThreadContexts#propagateから呼ばれる
 */
public final class JobContextPropagator implements ThreadContextPropagator {

    @Override
    public Runnable wrap(final Runnable task) {
        return JobContext.propagate(task);
    }
}
