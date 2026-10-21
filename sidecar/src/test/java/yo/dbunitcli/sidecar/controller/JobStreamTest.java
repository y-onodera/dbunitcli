package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.json.JsonMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.job.Interruptible;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * {@code @Interruptible} エンドポイントを Accept: text/event-stream で呼んだときのSSE（JobStreamFilter）。
 * filterが応答を返す前にchainを進める作りはMicronautのfilterの実行順序に依存するため、版上げ時の回帰確認も兼ねる。
 */
@MicronautTest
@Property(name = FileResources.PROPERTY_WORKSPACE, value = "target/test-temp/workspace/sample")
@Property(name = "job-stream-test", value = "true")
class JobStreamTest {

    // tauriと同じAccept。対象外のエンドポイントが406にならないよう、ワイルドカードを低い優先度で併記する
    private static final String ACCEPT_STREAM = "text/event-stream, */*;q=0.1";

    private static final String WORKSPACE = "target/test-temp/workspace/sample";

    static volatile CountDownLatch started;

    static volatile CountDownLatch interrupted;

    static volatile CountDownLatch logReceived;

    @Inject
    EmbeddedServer server;

    @Inject
    JsonMapper json;

    @Requires(property = "job-stream-test", value = "true")
    @Controller("/job-stream-test")
    static class TestController implements ControllerExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger("JobStreamTest");

        /** count回ログ（silentなら出力無し）を出しながら待つ。logReceivedがあればクライアントの受信まで完了しない */
        @Interruptible
        @Post(uri = "work", produces = MediaType.APPLICATION_JSON)
        public String work(@QueryValue final int count, @QueryValue(defaultValue = "false") final boolean silent,
                           @QueryValue(defaultValue = "false") final boolean waitLog) {
            started.countDown();
            try {
                for (int i = 0; i < count; i++) {
                    if (!silent) {
                        LOGGER.info("line{}", i);
                    }
                    if (waitLog && !logReceived.await(20, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("log was not pushed before completion");
                    }
                    Thread.sleep(100);
                }
            } catch (final InterruptedException e) {
                interrupted.countDown();
                throw new ApplicationException(e);
            }
            return "{\"done\":true}";
        }
    }

    @BeforeEach
    public void setUp() {
        started = new CountDownLatch(1);
        interrupted = new CountDownLatch(1);
        logReceived = new CountDownLatch(1);
    }

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
    public void testStream_結果は通常の応答と同じ本文をresultで返す() throws Exception {
        final Stream stream = this.stream("dbunit-cli/scaffold/exec",
                "{\"-target\":\"ddl\",\"-template\":\"scaffoldTpl\",\"-unitSetting\":\"scaffoldUnit\""
                        + ",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}");

        Assertions.assertTrue(stream.contentType().startsWith(MediaType.TEXT_EVENT_STREAM), stream.contentType());
        Assertions.assertEquals(200, stream.result().get("status"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-exec-response.json"),
                (String) stream.result().get("body"));
    }

    @Test
    public void testStream_継承したexecのログを流す() throws Exception {
        final Stream stream = this.stream("dbunit-cli/run/exec", "{\"name\":\"none\",\"input\":{\"-unknown\":\"x\"}}");

        Assertions.assertEquals(200, stream.result().get("status"));
        Assertions.assertTrue(stream.logs().stream().anyMatch(line -> line.contains("INFO  AbstractCommandController - ")),
                "コントローラのログが流れること: " + stream.logs());
        Assertions.assertTrue(stream.logs().stream().anyMatch(line -> line.contains("INFO  ComparableDataSetLoader - ")),
                "core内のログも流れること: " + stream.logs());
    }

    @Test
    public void testStream_エラー応答もステータスと本文をresultで返す() throws Exception {
        final Stream stream = this.stream("dbunit-cli/dataset-setting/generate", "{}");

        Assertions.assertEquals(400, stream.result().get("status"));
        Assertions.assertEquals("Fix Input Parameter", stream.result().get("reason"));
        Assertions.assertTrue(((String) stream.result().get("body")).contains("-src.src is required"), stream.result().toString());
    }

    @Test
    public void testStream_対象外のエンドポイントは通常の応答を返す() throws Exception {
        try (HttpClient http = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = http.send(HttpRequest.newBuilder(this.uri("dbunit-cli/workspace/resources"))
                    .header("Accept", ACCEPT_STREAM)
                    .GET()
                    .build(), HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith(MediaType.APPLICATION_JSON));
        }
    }

    @Test
    public void testStream_Acceptが通常なら対象のエンドポイントも通常の応答を返す() throws Exception {
        try (HttpClient http = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = http.send(HttpRequest.newBuilder(this.uri("dbunit-cli/job-stream-test/work?count=1"))
                    .header("Accept", "*/*")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.ofString());

            Assertions.assertEquals(200, response.statusCode());
            Assertions.assertEquals("{\"done\":true}", response.body());
        }
    }

    @Test
    public void testStream_ログは処理の完了を待たずにpushされる() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=1&waitLog=true")) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && !line.startsWith("data:")) {
                // 最初のログまで読む
            }
            Assertions.assertNotNull(line);
            Assertions.assertTrue(line.endsWith("line0"), line);
            logReceived.countDown();
            while ((line = reader.readLine()) != null && !line.startsWith("event: result")) {
                // 完了まで読む
            }
            Assertions.assertNotNull(line, "ログを受け取った後に処理が完了すること");
        }
    }

    @Test
    public void testStream_ログ受信中に切断すると実行スレッドへinterruptを送る() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=300")) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null && !line.startsWith("data:")) {
                // 最初のログまで読む
            }
            Assertions.assertNotNull(line);
        }
        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "切断で中断されること");
    }

    @Test
    public void testStream_出力の無い処理中に切断してもハートビートで検知して中断する() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=300&silent=true")) {
            Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));
            Thread.sleep(500);
        }
        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "ハートビートの書き込みで切断に気付くこと");
    }

    private record Stream(String contentType, List<String> logs, Map<String, Object> result) {
    }

    @SuppressWarnings("unchecked")
    private Stream stream(final String path, final String body) throws Exception {
        try (HttpClient http = HttpClient.newHttpClient()) {
            final HttpResponse<String> response = http.send(HttpRequest.newBuilder(this.uri(path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", ACCEPT_STREAM)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build(), HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode());
            final List<String> logs = new ArrayList<>();
            Map<String, Object> result = null;
            for (final String block : response.body().replace("\r\n", "\n").split("\n\n")) {
                String name = null;
                final List<String> data = new ArrayList<>();
                for (final String line : block.split("\n")) {
                    if (line.startsWith("event:")) {
                        name = line.substring("event:".length()).trim();
                    } else if (line.startsWith("data:")) {
                        data.add(line.substring("data:".length()).stripLeading());
                    }
                }
                if ("log".equals(name)) {
                    Assertions.assertNull(result, "resultの後にlogが流れないこと");
                    logs.add(String.join("\n", data));
                } else if ("result".equals(name)) {
                    result = this.json.readValue(String.join("\n", data), Map.class);
                }
            }
            Assertions.assertNotNull(result, "最後にresultが流れること: " + response.body());
            return new Stream(response.headers().firstValue("Content-Type").orElse(""), logs, result);
        }
    }

    private Socket open(final String path) throws IOException {
        final Socket socket = new Socket("localhost", this.server.getPort());
        final OutputStream out = socket.getOutputStream();
        out.write(("POST /" + path + " HTTP/1.1\r\nHost: localhost\r\nAccept: " + ACCEPT_STREAM
                + "\r\nContent-Length: 0\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        return socket;
    }

    private URI uri(final String path) {
        return URI.create(this.server.getURL() + "/" + path);
    }
}
