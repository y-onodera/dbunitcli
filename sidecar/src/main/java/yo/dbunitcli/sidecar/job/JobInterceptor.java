package yo.dbunitcli.sidecar.job;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.http.context.ServerRequestContext;
import jakarta.inject.Singleton;

/**
 * {@link Interruptible} の付いたメソッドを、{@link JobStreamFilter} がrequest属性に置いたジョブとして実行する。
 * ジョブが無ければ（SSEで呼ばれていなければ）そのまま実行する。
 */
@Singleton
@InterceptorBean(Interruptible.class)
public class JobInterceptor implements MethodInterceptor<Object, Object> {

    @Override
    public Object intercept(final MethodInvocationContext<Object, Object> context) {
        final JobContext job = ServerRequestContext.currentRequest()
                .flatMap(request -> request.getAttribute(JobContext.ATTRIBUTE, JobContext.class))
                .orElse(null);
        if (job == null) {
            return context.proceed();
        }
        return job.run(context::proceed);
    }
}
