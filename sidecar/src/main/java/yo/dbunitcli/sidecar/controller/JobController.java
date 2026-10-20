package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.sse.Event;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.reactivestreams.Publisher;
import yo.dbunitcli.sidecar.dto.JobEventDto;
import yo.dbunitcli.sidecar.job.JobManager;

/**
 * 各コントローラの {@code .../job} で開始したジョブの進捗参照・中断API。
 * events はSSEでログ行（event: log）と完了（event: done）をpushする。
 */
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/job")
public class JobController implements ControllerExceptionHandler {

    private final JobManager jobs;

    public JobController(final JobManager jobs) {
        this.jobs = jobs;
    }

    @Get(uri = "{id}/events", produces = MediaType.TEXT_EVENT_STREAM)
    public Publisher<Event<JobEventDto>> events(@PathVariable final String id) {
        return this.jobs.find(id)
                .map(job -> job.events().map(event -> Event.of(event).name(event.getType())))
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "job not found: " + id));
    }

    @Post(uri = "{id}/cancel", produces = MediaType.TEXT_PLAIN)
    public String cancel(@PathVariable final String id) {
        this.jobs.cancel(id);
        return "success";
    }
}
