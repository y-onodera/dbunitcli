package yo.dbunitcli.sidecar.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
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
import yo.dbunitcli.sidecar.job.JobStreamFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Client("/")
    HttpClient client;

    @Inject
    JsonMapper json;

    @Requires(property = "job-stream-test", value = "true")
    @Controller("/job-stream-test")
    static class TestController implements ControllerExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger("JobStreamTest");

        /** count回ログ（silentなら出力無し）を出しながら待つ。waitLogならクライアントの受信まで完了しない */
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
        Files.deleteIfExists(Paths.get(WORKSPACE, "target/convert/parallel/multi1.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "target/convert/parallel/multi2.csv"));
        Files.deleteIfExists(Paths.get(WORKSPACE, "target/convert/parallel"));
    }

    @Test
    public void testStream_結果は通常の応答と同じ本文をresultで返す() throws Exception {
        final Stream stream = this.stream(HttpRequest.POST("dbunit-cli/scaffold/exec",
                "{\"-target\":\"ddl\",\"-template\":\"scaffoldTpl\",\"-unitSetting\":\"scaffoldUnit\""
                        + ",\"-dataset.src\":\"resources/src/csv\",\"-dataset.srcType\":\"csv\"}"));

        Assertions.assertEquals(200, stream.result().get("status"));
        JsonTestHelper.assertJsonEquals(
                Paths.get("src/test/resources/yo/dbunitcli/sidecar/controller/scaffold-exec-response.json"),
                (String) stream.result().get("body"));
    }

    @Test
    public void testStream_継承したexecのログを流す() throws Exception {
        final Stream stream = this.stream(HttpRequest.POST("dbunit-cli/run/exec", "{\"name\":\"none\",\"input\":{\"-unknown\":\"x\"}}")
                .header(JobStreamFilter.LOG_HEADER, "true"));

        Assertions.assertEquals(200, stream.result().get("status"));
        Assertions.assertTrue(stream.logs().stream().anyMatch(line -> line.contains("INFO  AbstractCommandController - ")),
                "コントローラのログが流れること: " + stream.logs());
        Assertions.assertTrue(stream.logs().stream().anyMatch(line -> line.contains("INFO  ComparableDataSetLoader - ")),
                "core内のログも流れること: " + stream.logs());
    }

    @Test
    public void testStream_並列読込の作業スレッドのログも流す() throws Exception {
        final Stream stream = this.stream(HttpRequest.POST("dbunit-cli/convert/exec",
                        "{\"name\":\"parallel\",\"input\":{\"-src.src\":\"resources/src/csv\",\"-src.srcType\":\"csv\""
                                + ",\"-result.result\":\"target/convert/parallel\",\"-result.resultType\":\"csv\",\"-result.parallel\":\"2\"}}")
                .header(JobStreamFilter.LOG_HEADER, "true"));

        Assertions.assertEquals(200, stream.result().get("status"));
        Assertions.assertTrue(stream.logs().stream().anyMatch(line -> line.contains("parallel lanes=2, threads=2")),
                "並列に読み込むこと: " + stream.logs());
        Assertions.assertEquals(2, stream.logs().stream().filter(line -> line.contains("ComparableCsvDataSetProducer - produce - start")).count(),
                "作業スレッドで出力したログも流れること: " + stream.logs());
    }

    @Test
    public void testStream_ログを求めなければログは流さない() throws Exception {
        final Stream stream = this.stream(HttpRequest.POST("dbunit-cli/run/exec", "{\"name\":\"none\",\"input\":{\"-unknown\":\"x\"}}"));

        Assertions.assertEquals(200, stream.result().get("status"));
        Assertions.assertEquals(List.of(), stream.logs());
    }

    @Test
    public void testStream_エラー応答もステータスと本文をresultで返す() throws Exception {
        final Stream stream = this.stream(HttpRequest.POST("dbunit-cli/dataset-setting/generate", "{}"));

        Assertions.assertEquals(400, stream.result().get("status"));
        Assertions.assertEquals("Fix Input Parameter", stream.result().get("reason"));
        Assertions.assertTrue(((String) stream.result().get("body")).contains("-src.src is required"), stream.result().toString());
    }

    @Test
    public void testStream_対象外のエンドポイントは通常の応答を返す() {
        final HttpResponse<String> response = this.client.toBlocking().exchange(
                HttpRequest.GET("dbunit-cli/workspace/resources").accept(ACCEPT_STREAM), String.class);

        Assertions.assertEquals(200, response.code());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getContentType().orElseThrow());
    }

    @Test
    public void testStream_Acceptが通常なら対象のエンドポイントも通常の応答を返す() {
        final String response = this.client.toBlocking().retrieve(
                HttpRequest.POST("dbunit-cli/job-stream-test/work?count=1", "").accept(MediaType.ALL));

        Assertions.assertEquals("{\"done\":true}", response);
    }

    @Test
    public void testStream_ログは処理の完了を待たずにpushされる() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=1&waitLog=true", true)) {
            final BufferedReader reader = reader(socket);
            final String log = readUntil(reader, "data:");
            Assertions.assertNotNull(log);
            Assertions.assertTrue(log.endsWith("line0"), log);
            logReceived.countDown();
            Assertions.assertNotNull(readUntil(reader, "event: result"), "ログを受け取った後に処理が完了すること");
        }
    }

    @Test
    public void testStream_ログ受信中に切断すると実行スレッドへinterruptを送る() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=300", true)) {
            Assertions.assertNotNull(readUntil(reader(socket), "data:"));
        }
        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "切断で中断されること");
    }

    @Test
    public void testStream_出力の無い処理中に切断してもハートビートで検知して中断する() throws Exception {
        try (Socket socket = this.open("dbunit-cli/job-stream-test/work?count=300&silent=true", false)) {
            Assertions.assertTrue(started.await(10, TimeUnit.SECONDS));
            Thread.sleep(500);
        }
        Assertions.assertTrue(interrupted.await(10, TimeUnit.SECONDS), "ハートビートの書き込みで切断に気付くこと");
    }

    private record Stream(List<String> logs, Map<String, Object> result) {
    }

    @SuppressWarnings("unchecked")
    private Stream stream(final MutableHttpRequest<String> request) throws IOException {
        final String body = this.client.toBlocking().retrieve(
                request.accept(ACCEPT_STREAM).contentType(MediaType.APPLICATION_JSON_TYPE));
        final List<String> logs = new ArrayList<>();
        Map<String, Object> result = null;
        for (final String block : body.split("\n\n")) {
            final List<String> lines = List.of(block.split("\n"));
            final String data = lines.stream()
                    .filter(line -> line.startsWith("data:"))
                    .map(line -> line.substring("data:".length()).stripLeading())
                    .collect(Collectors.joining("\n"));
            if (lines.contains("event: log")) {
                Assertions.assertNull(result, "resultの後にlogが流れないこと");
                logs.add(data);
            } else if (lines.contains("event: result")) {
                result = this.json.readValue(data, Map.class);
            }
        }
        Assertions.assertNotNull(result, "最後にresultが流れること: " + body);
        return new Stream(logs, result);
    }

    private Socket open(final String path, final boolean logging) throws IOException {
        final Socket socket = new Socket("localhost", this.server.getPort());
        final OutputStream out = socket.getOutputStream();
        out.write(("POST /" + path + " HTTP/1.1\r\nHost: localhost\r\nAccept: " + ACCEPT_STREAM
                + (logging ? "\r\n" + JobStreamFilter.LOG_HEADER + ": true" : "")
                + "\r\nContent-Length: 0\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
        return socket;
    }

    private static BufferedReader reader(final Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    /** prefixで始まる行まで読み進めてその行を返す。見つからずに閉じたらnull */
    private static String readUntil(final BufferedReader reader, final String prefix) throws IOException {
        String line;
        while ((line = reader.readLine()) != null && !line.startsWith(prefix)) {
            // 読み飛ばす
        }
        return line;
    }
}
