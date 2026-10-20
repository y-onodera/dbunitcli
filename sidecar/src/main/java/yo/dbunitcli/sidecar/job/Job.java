package yo.dbunitcli.sidecar.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import yo.dbunitcli.sidecar.controller.ApplicationException;
import yo.dbunitcli.sidecar.dto.JobEventDto;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 仮想スレッドで実行する1件の処理。実行中のログと完了結果をSSE用のイベントとして保持する。
 * 失敗時のstatus/reason/messageは同期APIのControllerExceptionHandlerと揃える。
 */
public class Job {

    public enum Status {RUNNING, SUCCEEDED, FAILED, CANCELLED}

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    /** 遅れて購読したクライアントへ再送するイベント数の上限（超えた分は古いログから破棄、完了イベントは常に残る） */
    private static final int REPLAY_LIMIT = 5000;

    private final String id;

    private final Sinks.Many<JobEventDto> events = Sinks.many().replay().limit(REPLAY_LIMIT);

    private Status status = Status.RUNNING;

    private long finishedAt;

    private Thread thread;

    Job(final String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public synchronized Status getStatus() {
        return this.status;
    }

    public Flux<JobEventDto> events() {
        return this.events.asFlux();
    }

    synchronized void start(final Callable<String> task) {
        if (this.status != Status.RUNNING) {
            return;
        }
        this.thread = Thread.ofVirtual().name("job-" + this.id).unstarted(() -> {
            MDC.put(JobManager.MDC_JOB_ID, this.id);
            try {
                final String result = task.call();
                this.finish(Status.SUCCEEDED, JobEventDto.done(Status.SUCCEEDED.name(), 200, "OK", result, null));
            } catch (final ApplicationException ex) {
                this.finish(Status.FAILED, JobEventDto.done(Status.FAILED.name(), 400, "Fix Input Parameter",
                        null, "Execution failed. cause: " + ex.getMessage()));
            } catch (final IOException ex) {
                this.finish(Status.FAILED, JobEventDto.done(Status.FAILED.name(), 500, "File Operation Failed",
                        null, "File operation failed. cause: " + ex.getMessage()));
            } catch (final Throwable th) {
                LOGGER.error("cause:", th);
                this.finish(Status.FAILED, JobEventDto.done(Status.FAILED.name(), 500, "Internal Server Error",
                        null, th.toString()));
            } finally {
                MDC.remove(JobManager.MDC_JOB_ID);
            }
        });
        this.thread.start();
    }

    /** 実行中なら中断扱いにして仮想スレッドへinterruptを送る（割り込みを確認しない処理は最後まで動く） */
    synchronized void cancel() {
        if (this.finish(Status.CANCELLED, JobEventDto.done(Status.CANCELLED.name(), 499, "Cancelled", null, "Execution cancelled."))) {
            if (this.thread != null) {
                this.thread.interrupt();
            }
        }
    }

    synchronized void log(final String message) {
        if (this.status == Status.RUNNING) {
            this.events.tryEmitNext(JobEventDto.log(message));
        }
    }

    synchronized boolean isFinishedBefore(final long time) {
        return this.status != Status.RUNNING && this.finishedAt < time;
    }

    private synchronized boolean finish(final Status status, final JobEventDto done) {
        if (this.status != Status.RUNNING) {
            return false;
        }
        this.status = status;
        this.finishedAt = System.currentTimeMillis();
        this.events.tryEmitNext(done);
        this.events.tryEmitComplete();
        return true;
    }
}
