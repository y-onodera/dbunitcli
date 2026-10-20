package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * クライアントが発行したjobIdごとに、処理中のログ配信と中断を管理する。
 * 処理の結果は通常のHTTP応答で返すため、ここでは扱わない。
 */
@Singleton
public class JobManager {

    static final String MDC_JOB_ID = "jobId";

    /** 完了したジョブのログを、遅れて購読したクライアント向けに保持する時間 */
    private static final long RETENTION_MILLIS = TimeUnit.MINUTES.toMillis(10);

    /**
     * 処理より先に届いたcancel・購読を待つ時間。
     * tauriはabort時に対象外のエンドポイントにもcancelを送るため、実行されないまま残る分は早めに破棄する
     */
    private static final long PENDING_MILLIS = TimeUnit.MINUTES.toMillis(1);

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    private final JobLogAppender appender;

    public JobManager() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.appender = new JobLogAppender(this);
        this.appender.setContext(context);
        this.appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.appender);
    }

    /**
     * 現在のスレッドでtaskをjobIdのジョブとして実行し、中断の対象にする。
     * loggingがtrueならログを /job/{id}/events 用に保持する。falseなら完了時にすぐ破棄する
     */
    public <T> T run(final String jobId, final boolean logging, final Supplier<T> task) {
        final Job job = this.job(jobId);
        job.attach(Thread.currentThread(), logging);
        MDC.put(MDC_JOB_ID, jobId);
        try {
            return task.get();
        } finally {
            MDC.remove(MDC_JOB_ID);
            job.finish();
            if (!logging) {
                this.jobs.remove(jobId, job);
            }
            // 中断用の割り込みが処理完了と競合して残った場合に、後続処理へ持ち越さない
            Thread.interrupted();
        }
    }

    /** jobIdのログ行。処理の完了で終端する */
    public Flux<String> logs(final String jobId) {
        return this.job(jobId).logs();
    }

    public void cancel(final String jobId) {
        this.job(jobId).cancel();
    }

    Optional<Job> find(final String jobId) {
        return Optional.ofNullable(this.jobs.get(jobId));
    }

    private Job job(final String jobId) {
        final long now = System.currentTimeMillis();
        this.jobs.values().removeIf(job -> job.isExpired(now - RETENTION_MILLIS, now - PENDING_MILLIS));
        return this.jobs.computeIfAbsent(jobId, id -> new Job());
    }

    @PreDestroy
    public void close() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(this.appender);
        this.appender.stop();
        this.jobs.values().forEach(Job::cancel);
    }
}
