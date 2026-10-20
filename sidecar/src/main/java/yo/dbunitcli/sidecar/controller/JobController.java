package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.sse.Event;
import org.reactivestreams.Publisher;
import yo.dbunitcli.sidecar.job.JobManager;

/**
 * X-Job-Id 付きで呼んだ {@code @JobTracked} エンドポイントの、実行中ログ配信と中断API。
 * 結果は元のエンドポイントの応答で返る。
 */
@Controller("/job")
public class JobController {

    private final JobManager jobs;

    public JobController(final JobManager jobs) {
        this.jobs = jobs;
    }

    /** ログ行を event: log で push し、処理が終わったら閉じる。処理の開始前に購読してもよい */
    @Get(uri = "{id}/events", produces = MediaType.TEXT_EVENT_STREAM)
    public Publisher<Event<String>> events(@PathVariable final String id) {
        return this.jobs.logs(id).map(line -> Event.of(line).name("log"));
    }

    @Post(uri = "{id}/cancel", produces = MediaType.TEXT_PLAIN)
    public String cancel(@PathVariable final String id) {
        this.jobs.cancel(id);
        return "success";
    }
}
