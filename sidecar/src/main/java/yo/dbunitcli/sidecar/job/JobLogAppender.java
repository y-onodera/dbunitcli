package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * MDCにjobIdを持つログイベントを、該当ジョブのイベントへ転送するappender。
 * MDCはスレッド単位のため、core内部で別スレッドから出力されたログは対象外になる。
 */
class JobLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final JobManager jobs;

    private final PatternLayout layout = new PatternLayout();

    JobLogAppender(final JobManager jobs) {
        this.jobs = jobs;
        this.setName("job");
    }

    @Override
    public void start() {
        this.layout.setContext(this.getContext());
        this.layout.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{0} - %msg%n");
        this.layout.start();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.layout.stop();
    }

    @Override
    protected void append(final ILoggingEvent event) {
        final String jobId = event.getMDCPropertyMap().get(JobManager.MDC_JOB_ID);
        if (jobId != null) {
            this.jobs.find(jobId).ifPresent(job -> job.log(this.layout.doLayout(event).stripTrailing()));
        }
    }
}
