package yo.dbunitcli.resource;

/**
 * テスト用。{@link #VALUE}が束縛されていれば作業スレッドへ引き継ぐ（束縛されていなければ何もしない）
 */
public final class TestThreadContextPropagator implements ThreadContextPropagator {

    public static final ScopedValue<String> VALUE = ScopedValue.newInstance();

    @Override
    public Runnable wrap(final Runnable task) {
        if (!VALUE.isBound()) {
            return task;
        }
        final String value = VALUE.get();
        return () -> ScopedValue.where(VALUE, value).run(task);
    }
}
