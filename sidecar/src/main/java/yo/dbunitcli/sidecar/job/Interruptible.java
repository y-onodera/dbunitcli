package yo.dbunitcli.sidecar.job;

import io.micronaut.aop.Around;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 実行スレッドへ割り込んで中断しても安全なエンドポイント（読み取りのみ、または出力を作り直せる処理）に付ける。
 * リクエストに {@link JobInterceptor#HEADER} があれば /job/{id}/cancel で中断でき、
 * {@link JobInterceptor#LOG_HEADER} もあれば実行中のログを /job/{id}/events へ流す。
 * 設定ファイルを書き換える処理（save・delete・rename等）には付けない（中断で中途半端な状態が残りうる）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Around
public @interface Interruptible {
}
