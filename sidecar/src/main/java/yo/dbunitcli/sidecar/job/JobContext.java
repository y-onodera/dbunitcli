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

    /** ログの振り分け（JobLogAppender）用。runの範囲内だけ束縛される。core内部の別スレッドへはpropagateで包んだタスクだけ引き継ぐ */
    private static final ScopedValue<JobContext> CURRENT = ScopedValue.newInstance();

    /** ログを求められていなければnull */
    private final Consumer<String> logs;

    private Thread thread;

    private boolean cancelled;

    JobContext(final Consumer<String> logs) {
        this.logs = logs;
    }

    /** 中断の対象にするだけで、ログは流さないジョブ */
    static JobContext withoutLogs() {
        return new JobContext(null);
    }

    static Optional<JobContext> current() {
        // ScopedValue#orElse はnullを受け付けないため、束縛の有無で判定する
        return CURRENT.isBound() ? Optional.of(CURRENT.get()) : Optional.empty();
    }

    /**
     * 現在のスレッドのジョブを、別スレッドで実行されるtaskのログ振り分け先として引き継ぐ。
     * 中断は投入元のスレッドへの割り込みを契機に、投入元が作業スレッドへ伝える
     */
    static Runnable propagate(final Runnable task) {
        return current().<Runnable>map(job -> () -> ScopedValue.where(CURRENT, job).run(task))
                        .orElse(task);
    }

    /** 現在のスレッドでtaskを実行し、中断とログ振り分けの対象にする */
    <T> T run(final Supplier<T> task) {
        this.attach(Thread.currentThread());
        try {
            return ScopedValue.where(CURRENT, this).call(task::get);
        } finally {
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

    /** ログを流す必要があるか。求められていない、または中断済み（送り先が無い）なら整形を省くために使う */
    synchronized boolean wantsLogs() {
        return this.logs != null && !this.cancelled;
    }

    void log(final String line) {
        if (this.logs != null) {
            this.logs.accept(line);
        }
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
