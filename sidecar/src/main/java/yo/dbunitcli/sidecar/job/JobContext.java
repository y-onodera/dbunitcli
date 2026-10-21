package yo.dbunitcli.sidecar.job;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 1リクエスト分のジョブ。{@link JobStreamFilter} が作って request 属性で {@link JobInterceptor} へ渡し、
 * 実行スレッド（中断用）とログの流し先を保持する。リクエストが終われば参照されなくなる。
 */
final class JobContext {

    static final String ATTRIBUTE = "dbunitcli.job";

    /** ログの振り分け（JobLogAppender）用。MDCと同じくスレッド単位のため、core内部で別スレッドから出力されたログは対象外 */
    private static final ThreadLocal<JobContext> CURRENT = new ThreadLocal<>();

    private final Consumer<String> logs;

    private Thread thread;

    private boolean cancelled;

    JobContext(final Consumer<String> logs) {
        this.logs = logs;
    }

    static Optional<JobContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /** 現在のスレッドでtaskを実行し、中断とログ振り分けの対象にする */
    <T> T run(final Supplier<T> task) {
        this.attach(Thread.currentThread());
        CURRENT.set(this);
        try {
            return task.get();
        } finally {
            CURRENT.remove();
            this.detach();
            // 中断用の割り込みが処理完了と競合して残った場合に、後続処理へ持ち越さない
            Thread.interrupted();
        }
    }

    /** 実行中スレッドへinterruptを送る（割り込みを確認しない処理は最後まで動く）。開始前なら開始時に割り込む */
    synchronized void cancel() {
        this.cancelled = true;
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    void log(final String line) {
        this.logs.accept(line);
    }

    private synchronized void attach(final Thread thread) {
        this.thread = thread;
        if (this.cancelled) {
            thread.interrupt();
        }
    }

    private synchronized void detach() {
        this.thread = null;
    }
}
