package yo.dbunitcli.sidecar.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import yo.dbunitcli.sidecar.controller.ApplicationException;
import yo.dbunitcli.sidecar.controller.ControllerExceptionHandler.ErrorResult;
import yo.dbunitcli.sidecar.dto.JobEventDto;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * 仮想スレッドで実行する1件の処理。実行中のログと完了結果をSSE用のイベントとして保持する。
 */
public class Job {

    enum Status {RUNNING, SUCCEEDED, FAILED, CANCELLED}

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
                this.finish(Status.SUCCEEDED, 200, "OK", task.call(), null);
            } catch (final ApplicationException ex) {
                this.fail(ErrorResult.of(ex));
            } catch (final IOException ex) {
                this.fail(ErrorResult.of(ex));
            } catch (final Throwable th) {
                LOGGER.error("cause:", th);
                this.finish(Status.FAILED, 500, "Internal Server Error", null, th.toString());
            } finally {
                MDC.remove(JobManager.MDC_JOB_ID);
            }
        });
        this.thread.start();
    }

    /** 実行中なら中断扱いにして仮想スレッドへinterruptを送る（割り込みを確認しない処理は最後まで動く） */
    synchronized void cancel() {
        final Thread running = this.thread;
        if (this.finish(Status.CANCELLED, 499, "Cancelled", null, "Execution cancelled.") && running != null) {
            running.interrupt();
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

    private void fail(final ErrorResult error) {
        this.finish(Status.FAILED, error.status().getCode(), error.reason(), null, error.message());
    }

    private synchronized boolean finish(final Status status, final int httpStatus, final String reason,
                                        final String result, final String message) {
        if (this.status != Status.RUNNING) {
            return false;
        }
        this.status = status;
        this.finishedAt = System.currentTimeMillis();
        this.thread = null;
        this.events.tryEmitNext(JobEventDto.done(status.name(), httpStatus, reason, result, message));
        this.events.tryEmitComplete();
        return true;
    }
}
