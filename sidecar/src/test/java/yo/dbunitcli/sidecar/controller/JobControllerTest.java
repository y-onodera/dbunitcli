package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.job.JobInterceptor;
import yo.dbunitcli.sidecar.job.JobManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    public void testJobTracked_ヘッダ付きでも結果は通常の応答で返る() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/scaffold/exec",
                                "{\"-target\":\"ddl\",\"-template\":\"scaffoldTpl\",\"-unitSetting\":\"scaffoldUnit\""
                                        + ",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}")
                        .header(JobInterceptor.HEADER, jobId)
                        .header(JobInterceptor.LOG_HEADER, "true"));

        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-exec-response.json"),
                response);
        Assertions.assertEquals(200, this.sse(jobId).statusCode(), "処理の完了後に購読してもイベントが閉じること");
    }

    @Test
    public void testJobTracked_継承したexecのログもjobIdへ振り分ける() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final CompletableFuture<HttpResponse<String>> events = this.sseAsync(jobId);
        Assertions.assertThrows(HttpClientResponseException.class, () -> this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/run/exec", "{\"name\":\"none\",\"input\":{\"-unknown\":\"x\"}}")
                        .header(JobInterceptor.HEADER, jobId)
                        .header(JobInterceptor.LOG_HEADER, "true")));

        final String body = events.get(30, TimeUnit.SECONDS).body();
        Assertions.assertTrue(body.contains("event: log"), body);
        Assertions.assertTrue(body.contains("INFO  AbstractCommandController - "), "コントローラのログが流れること: " + body);
        Assertions.assertTrue(body.contains("INFO  ComparableDataSetLoader - "), "core内のログも流れること: " + body);
    }

    @Test
    public void testEvents_ログは処理の完了を待たずにpushされる() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final CountDownLatch received = new CountDownLatch(1);
        final CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> this.jobs.run(jobId, true, () -> {
            LoggerFactory.getLogger("JobControllerTest").info("first line");
            // クライアントがログを受け取るまで完了しない（逐次pushされなければタイムアウトで失敗する）
            try {
                if (!received.await(20, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("log was not pushed before completion");
                }
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "done";
        }));
        try (java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient()) {
            final HttpResponse<Stream<String>> response = http.send(this.sseRequest(jobId),
                    HttpResponse.BodyHandlers.ofLines());
            final Iterator<String> lines = response.body().iterator();
            final List<String> data = new ArrayList<>();
            while (lines.hasNext()) {
                final String line = lines.next();
                if (line.startsWith("data:")) {
                    data.add(line);
                    received.countDown();
                }
            }
            Assertions.assertEquals(1, data.size(), data.toString());
            Assertions.assertTrue(data.get(0).endsWith("first line"), data.get(0));
        }
        Assertions.assertEquals("done", result.get(30, TimeUnit.SECONDS));
    }

    @Test
    public void testCancel_実行中のジョブへinterruptを送る() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final CountDownLatch started = new CountDownLatch(1);
        final CompletableFuture<Boolean> interrupted = CompletableFuture.supplyAsync(() -> this.jobs.run(jobId, true, () -> {
            started.countDown();
            try {
                Thread.sleep(Duration.ofMinutes(1));
                return false;
            } catch (final InterruptedException e) {
                return true;
            }
        }));
        Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));

        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/job/" + jobId + "/cancel", ""));

        Assertions.assertEquals("success", response);
        Assertions.assertTrue(interrupted.get(10, TimeUnit.SECONDS));
    }

    private HttpResponse<String> sse(final String jobId) throws Exception {
        return this.sseAsync(jobId).get(30, TimeUnit.SECONDS);
    }

    private CompletableFuture<HttpResponse<String>> sseAsync(final String jobId) {
        final java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient();
        return http.sendAsync(this.sseRequest(jobId), HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> http.close());
    }

    private java.net.http.HttpRequest sseRequest(final String jobId) {
        return java.net.http.HttpRequest.newBuilder(
                        URI.create(this.server.getURL() + "/dbunit-cli/job/" + jobId + "/events"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/event-stream")
                .build();
    }
}
