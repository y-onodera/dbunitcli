package yo.dbunitcli.sidecar.job;

import io.micronaut.aop.Around;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 長時間化しうるエンドポイントに付ける。リクエストに {@link JobInterceptor#HEADER} があれば、
 * 実行中のログを /job/{id}/events へ流し、/job/{id}/cancel で中断できるようにする。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Around
public @interface JobTracked {
}
