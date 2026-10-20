package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.JobEventDto;
import yo.dbunitcli.sidecar.job.JobManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
class JobControllerTest {
    @Inject
    EmbeddedServer server;
    @Inject
    @Client("/")
    HttpClient client;
    @Inject
    JobManager jobs;

    private static final String WORKSPACE = "target/test-temp/workspace/sample";

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.stg"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.txt"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "resources/setting/scaffoldUnit.json"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src/multi1.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src/multi2.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "src"));
    }

    @Test
    public void testExecJob_SSEの完了イベントで同期execと同じ結果を返す() throws Exception {
        final String jobId = this.startJob("dbunit-cli/scaffold/exec/job",
                "{\"-target\":\"ddl\",\"-template\":\"scaffoldTpl\",\"-unitSetting\":\"scaffoldUnit\""
                        + ",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}");
        final JobEventDto done = this.lastEvent(jobId);

        Assertions.assertEquals("SUCCEEDED", done.status());
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-exec-response.json"),
                done.result());
        Assertions.assertTrue(Files.exists(Paths.get(WORKSPACE, "resources/template/scaffoldTpl.stg")));
    }

    @Test
    public void testEvents_ログは処理の完了を待たずにpushされる() throws Exception {
        final CountDownLatch received = new CountDownLatch(1);
        final Map<String, String> started = this.jobs.start(() -> {
            LoggerFactory.getLogger("JobControllerTest").info("first line");
            // クライアントがログを受け取るまで完了しない（逐次pushされなければタイムアウトで失敗する）
            if (!received.await(20, TimeUnit.SECONDS)) {
                throw new IllegalStateException("log was not pushed before completion");
            }
            return "done";
        });
        final String jobId = started.get("jobId");
        try (java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient()) {
            final HttpResponse<Stream<String>> response = http.send(java.net.http.HttpRequest.newBuilder(
                                    URI.create(this.server.getURL() + "/dbunit-cli/job/" + jobId + "/events"))
                            .header("Accept", "text/event-stream")
                            .build(),
                    HttpResponse.BodyHandlers.ofLines());
            final Iterator<String> lines = response.body().iterator();
            final List<String> data = new ArrayList<>();
            while (lines.hasNext()) {
                final String line = lines.next();
                if (line.startsWith("data:")) {
                    data.add(line);
                    if (line.contains("first line")) {
                        received.countDown();
                    }
                }
            }
            Assertions.assertEquals(2, data.size(), data.toString());
            Assertions.assertTrue(data.get(1).contains("\"status\":\"SUCCEEDED\""), data.get(1));
        }
    }

    @Test
    public void testExecJob_入力不備は完了イベントで400を返す() throws Exception {
        final String jobId = this.startJob("dbunit-cli/scaffold/exec/job", "{\"-target\":\"ddl\"}");
        final JobEventDto done = this.lastEvent(jobId);

        Assertions.assertEquals("FAILED", done.status());
        Assertions.assertEquals(400, done.httpStatus());
        Assertions.assertEquals("Fix Input Parameter", done.reason());
    }

    @Test
    public void testEvents_存在しないjobは404() throws Exception {
        final HttpResponse<String> response = this.sse("unknown");
        Assertions.assertEquals(404, response.statusCode());
    }

    @Test
    public void testCancel_完了済みjobへのcancelは結果を変えない() throws Exception {
        final String jobId = this.startJob("dbunit-cli/scaffold/exec/job", "{\"-target\":\"ddl\"}");
        this.lastEvent(jobId);
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/job/" + jobId + "/cancel", ""));

        Assertions.assertEquals("success", response);
        Assertions.assertEquals("FAILED", this.lastEvent(jobId).status());
    }

    @SuppressWarnings("unchecked")
    private String startJob(final String uri, final String body) throws IOException {
        final String response = this.client.toBlocking().retrieve(HttpRequest.POST(uri, body));
        final Map<String, String> started = ObjectMapper.getDefault().readValue(response, Map.class);
        final String jobId = started.get("jobId");
        Assertions.assertEquals("/dbunit-cli/job/" + jobId + "/events", started.get("events"));
        Assertions.assertEquals("/dbunit-cli/job/" + jobId + "/cancel", started.get("cancel"));
        return jobId;
    }

    private JobEventDto lastEvent(final String jobId) throws Exception {
        final HttpResponse<String> response = this.sse(jobId);
        Assertions.assertEquals(200, response.statusCode());
        final List<JobEventDto> events = new ArrayList<>();
        for (final String line : response.body().split("\n")) {
            if (line.startsWith("data:")) {
                events.add(ObjectMapper.getDefault().readValue(line.substring("data:".length()).trim(), JobEventDto.class));
            }
        }
        final JobEventDto last = events.get(events.size() - 1);
        Assertions.assertEquals(JobEventDto.TYPE_DONE, last.type());
        return last;
    }

    private HttpResponse<String> sse(final String jobId) throws Exception {
        try (java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient()) {
            return http.send(java.net.http.HttpRequest.newBuilder(
                                    URI.create(this.server.getURL() + "/dbunit-cli/job/" + jobId + "/events"))
                            .timeout(Duration.ofSeconds(30))
                            .header("Accept", "text/event-stream")
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
        }
    }
}
