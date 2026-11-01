package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * 出力単位ごとの書き出し処理。{@link GenerateStrategy#writer}がテンプレート準備を1回だけ行って返す。
 * unit=table等では複数スレッドから同時に呼ばれうる
 */
@FunctionalInterface
public interface GenerateWriter {
    void write(File resultFile, Parameter param) throws IOException;
}
