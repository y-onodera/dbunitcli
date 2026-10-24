package yo.dbunitcli.sidecar.job;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.sse.Event;
import io.micronaut.json.JsonMapper;
import io.micronaut.web.router.RouteAttributes;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Publisher;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import yo.dbunitcli.dataset.ParallelTableMapping;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link Interruptible} の付いたエンドポイントを Accept: text/event-stream で呼ぶと、
 * 最終結果（event: result）をSSEで返す。{@link #LOG_HEADER} があれば実行中のログ（event: log）も流す。
 * resultのdataは {"status","reason","body"} で、通常の応答のステータスと本文をそのまま運ぶ（エラー応答も同じ）。
 * クライアントが切断すると実行スレッドへinterruptを送る。それ以外の呼び出しは通常どおり処理する。
 */
@Filter("/**")
public class JobStreamFilter implements HttpServerFilter {

    /** 実行中のログを求めるときに付ける。無ければ中断の対象にするだけでログは整形・送信しない */
    public static final String LOG_HEADER = "X-Job-Log";

    private static final String LOG_EVENT = "log";

    private static final String RESULT_EVENT = "result";

    /**
     * Nettyは書き込み時にしか切断に気付かないため、ログが出ない間も定期的に書き込む。
     * 中断の遅れは最大でこの間隔の2倍程度になる
     */
    private static final Duration HEARTBEAT = Duration.ofSeconds(1);

    private final JsonMapper json;

    private final JobLogAppender appender = new JobLogAppender();

    public JobStreamFilter(final JsonMapper json) {
        this.json = json;
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.appender.setContext(context);
        this.appender.start();
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(this.appender);
        ParallelTableMapping.setTaskDecorator(JobContext::propagate);
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        if (!this.isTarget(request)) {
            return chain.proceed(request);
        }
        final Sinks.Many<Event<String>> events = Sinks.many().unicast().onBackpressureBuffer();
        final JobContext job = request.getHeaders().contains(LOG_HEADER)
                ? new JobContext(line -> emit(events, Event.of(line).name(LOG_EVENT)))
                : JobContext.withoutLogs();
        request.setAttribute(JobContext.ATTRIBUTE, job);
        // 応答を返した後にchainを進めるとControllerが実行されないため、応答を返す前に購読しておく
        final Disposable proceed = Flux.from(chain.proceed(request)).subscribe(
                response -> this.complete(events, response.code(), response.reason(), response.getBody().orElse(null)),
                error -> this.complete(events, 500, "Internal Server Error", new JsonError(String.valueOf(error.getMessage()))));
        final Flux<Event<String>> body = Flux.merge(events.asFlux(),
                        Flux.interval(HEARTBEAT).map(tick -> Event.<String>of("").comment("heartbeat")))
                .takeUntil(event -> RESULT_EVENT.equals(event.getName()))
                .doOnCancel(() -> {
                    job.cancel();
                    proceed.dispose();
                });
        return Flux.just(HttpResponse.ok(body).contentType(MediaType.TEXT_EVENT_STREAM_TYPE));
    }

    @PreDestroy
    public void close() {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(Logger.ROOT_LOGGER_NAME).detachAppender(this.appender);
        this.appender.stop();
        ParallelTableMapping.setTaskDecorator(null);
    }

    private boolean isTarget(final HttpRequest<?> request) {
        // 注釈はコンパイル時のメタデータで安く判定できるため、Acceptの解析より先に見る
        return RouteAttributes.getRouteInfo(request)
                              .map(route -> route.getAnnotationMetadata().hasAnnotation(Interruptible.class))
                              .orElse(false)
                && request.getHeaders().accept().stream().anyMatch(MediaType.TEXT_EVENT_STREAM_TYPE::equals);
    }

    private void complete(final Sinks.Many<Event<String>> events, final int status, final String reason, final Object body) {
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("reason", reason);
        result.put("body", this.toText(body));
        // ストリームはtakeUntilでresultの送出後に閉じる
        emit(events, Event.of(this.toText(result)).name(RESULT_EVENT));
    }

    private String toText(final Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof final CharSequence text) {
            return text.toString();
        }
        try {
            return this.json.writeValueAsString(body);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** ログ（実行スレッド）と結果（応答を受け取ったスレッド）は別スレッドから流れるため、送出を直列化する */
    private static void emit(final Sinks.Many<Event<String>> events, final Event<String> event) {
        synchronized (events) {
            events.tryEmitNext(event);
        }
    }
}
