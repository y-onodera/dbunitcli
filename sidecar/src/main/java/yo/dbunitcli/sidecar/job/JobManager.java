package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.LoggerContext;
import io.micronaut.serde.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public JobManager() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.appender = new JobLogAppender(this);
        this.appender.setContext(context);
        this.appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.appender);
    }

    /** 処理を開始し、{"jobId": "..."} を返す */
    public String start(final Callable<String> task) throws IOException {
        final long expired = System.currentTimeMillis() - RETENTION_MILLIS;
        this.jobs.values().removeIf(job -> job.isFinishedBefore(expired));
        final Job job = new Job(UUID.randomUUID().toString());
        this.jobs.put(job.getId(), job);
        job.start(task);
        return ObjectMapper.getDefault().writeValueAsString(Map.of("jobId", job.getId()));
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
