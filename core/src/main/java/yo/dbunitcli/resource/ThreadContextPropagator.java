package yo.dbunitcli.resource;

/**
 * 投入元スレッドの文脈（ScopedValue等）を取り出し、別スレッドでの実行時に張り直す。
 * META-INF/services/yo.dbunitcli.resource.ThreadContextPropagator に実装を登録すると、{@link ThreadContexts#propagate}で適用される
 */
public interface ThreadContextPropagator {

    /**
     * 投入元スレッドで呼ばれる。返したRunnableが作業スレッドで実行される
     */
    Runnable wrap(Runnable task);
}
