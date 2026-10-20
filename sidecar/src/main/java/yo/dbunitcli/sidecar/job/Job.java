package yo.dbunitcli.sidecar.job;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * X-Job-Id 1件分の状態。実行中スレッド（中断用）とログ行（SSE用）を保持する。
 * ログの購読と処理の開始はどちらが先でもよい（ログは再送用に保持する）。
 */
class Job {

    /** 遅れて購読したクライアントへ再送するログ行数の上限（超えた分は古い行から破棄） */
    private static final int REPLAY_LIMIT = 5000;

    private final Sinks.Many<String> logs = Sinks.many().replay().limit(REPLAY_LIMIT);

    private final long createdAt = System.currentTimeMillis();

    private Thread thread;

    private boolean cancelled;

    /** 0なら未完了 */
    private long finishedAt;

    Flux<String> logs() {
        return this.logs.asFlux();
    }

    /** 処理を実行するスレッドを登録する。開始前に中断済みなら、そのスレッドへ即座に割り込む */
    synchronized void attach(final Thread thread) {
        this.thread = thread;
        if (this.cancelled) {
            thread.interrupt();
        }
    }

    /** 実行中スレッドへinterruptを送る（割り込みを確認しない処理は最後まで動く） */
    synchronized void cancel() {
        this.cancelled = true;
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    synchronized void log(final String line) {
        this.logs.tryEmitNext(line);
    }

    synchronized boolean isFinished() {
        return this.finishedAt != 0;
    }

    synchronized void finish() {
        this.finishedAt = System.currentTimeMillis();
        this.thread = null;
        this.logs.tryEmitComplete();
    }

    /**
     * 破棄してよいか。完了済みはfinishedBefore以前に完了したもの、
     * 未実行（中断・ログ購読が処理より先に届いた、または対象外のエンドポイントだった）はcreatedBefore以前に作成されたもの
     */
    synchronized boolean isExpired(final long finishedBefore, final long createdBefore) {
        if (this.finishedAt != 0) {
            return this.finishedAt < finishedBefore;
        }
        return this.thread == null && this.createdAt < createdBefore;
    }
}
