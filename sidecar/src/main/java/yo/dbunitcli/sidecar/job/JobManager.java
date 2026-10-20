package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.LoggerContext;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Micronautのリクエストスレッドとは別の仮想スレッドで処理を実行し、jobIdで進捗・結果を参照させる。
 * HTTP接続を処理完了まで保持しないため、長時間の処理でも接続断の影響を受けない。
 */
@Singleton
public class JobManager {

    static final String MDC_JOB_ID = "jobId";

    /** 完了したジョブを保持する時間。経過後は次のジョブ開始時に破棄する */
    private static final long RETENTION_MILLIS = TimeUnit.MINUTES.toMillis(10);

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    private final JobLogAppender appender;

    private final String contextPath;

    public JobManager(@Value("${micronaut.server.context-path:}") final String contextPath) {
        this.contextPath = contextPath;
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.appender = new JobLogAppender(this);
        this.appender.setContext(context);
        this.appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.appender);
    }

    /** 処理を開始し、jobIdと進捗参照(events)・中断(cancel)のパスを返す */
    public Map<String, String> start(final Callable<String> task) {
        final long expired = System.currentTimeMillis() - RETENTION_MILLIS;
        this.jobs.values().removeIf(job -> job.isFinishedBefore(expired));
        final Job job = new Job(UUID.randomUUID().toString());
        this.jobs.put(job.getId(), job);
        job.start(task);
        final String base = this.contextPath + "/job/" + job.getId();
        return Map.of("jobId", job.getId(), "events", base + "/events", "cancel", base + "/cancel");
    }

    public Optional<Job> find(final String id) {
        return Optional.ofNullable(this.jobs.get(id));
    }

    public void cancel(final String id) {
        this.find(id).ifPresent(Job::cancel);
    }

    @PreDestroy
    public void close() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(this.appender);
        this.appender.stop();
        this.jobs.values().forEach(Job::cancel);
    }
}
