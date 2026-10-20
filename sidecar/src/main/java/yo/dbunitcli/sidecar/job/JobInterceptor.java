package yo.dbunitcli.sidecar.job;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.http.context.ServerRequestContext;
import jakarta.inject.Singleton;

/**
 * {@link JobTracked} の付いたメソッドを、リクエストの X-Job-Id に対応するジョブとして実行する。
 * ヘッダが無ければそのまま実行する。
 */
@Singleton
@InterceptorBean(JobTracked.class)
public class JobInterceptor implements MethodInterceptor<Object, Object> {

    public static final String HEADER = "X-Job-Id";

    /** ログを /job/{id}/events で購読するときに付ける。無ければ中断のみ受け付ける */
    public static final String LOG_HEADER = "X-Job-Log";

    private final JobManager jobs;

    public JobInterceptor(final JobManager jobs) {
        this.jobs = jobs;
    }

    @Override
    public Object intercept(final MethodInvocationContext<Object, Object> context) {
        return ServerRequestContext.currentRequest()
                .flatMap(request -> request.getHeaders().findFirst(HEADER)
                        .map(jobId -> this.jobs.run(jobId, request.getHeaders().contains(LOG_HEADER), context::proceed)))
                .orElseGet(context::proceed);
    }
}
