package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * ジョブの実行スレッドで出力されたログイベントを、そのジョブのイベントへ転送するappender。
 * 判定はスレッド単位のため、core内部で別スレッドから出力されたログは対象外になる
 * （ソースの並列読込の作業スレッドは JobContext#propagate で引き継ぐ）。
 */
class JobLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final PatternLayout layout = new PatternLayout();

    JobLogAppender() {
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
        // appenderはログ出力元のスレッドで同期的に呼ばれるため、そのスレッドのジョブを参照できる
        JobContext.current()
                  .filter(JobContext::wantsLogs)
                  .ifPresent(job -> job.log(this.layout.doLayout(event).stripTrailing()));
    }
}
