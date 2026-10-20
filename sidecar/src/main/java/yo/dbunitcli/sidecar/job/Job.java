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

    private boolean finished;

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
        if (!this.finished) {
            this.logs.tryEmitNext(line);
        }
    }

    synchronized void finish() {
        this.finished = true;
        this.finishedAt = System.currentTimeMillis();
        this.thread = null;
        this.logs.tryEmitComplete();
    }

    /** 完了から、または未実行のまま作成から一定時間経ったか */
    synchronized boolean isExpired(final long time) {
        return this.finished ? this.finishedAt < time : this.thread == null && this.createdAt < time;
    }
}
