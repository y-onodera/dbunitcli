package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.InlineSource;

import java.io.File;
import java.util.stream.Stream;

/**
 * SQLを読む形式（sql・csvq）。inlineSourceはSQL1つで、tableNameのテーブルとして読む
 */
public interface QueryDataSetProducer extends InlineSourceDataSetProducer {

    /**
     * sourceのSQLをテンプレートとして描画する。inlineSourceがあればsrcのファイルの代わりにその内容を使う
     */
    static String renderQuery(final ComparableDataSetParam param, final Source source, final Parameter parameter) {
        final InlineSource inlineSource = param.inlineSource();
        return inlineSource != null
                ? param.templateRender().render(inlineSource.content(), parameter)
                : param.templateRender().render(new File(source.filePath()), parameter);
    }

    Parameter parameter();

    /**
     * srcの各ファイルをSQL1つとして読む
     */
    @Override
    default Stream<? extends Source> getSrcFileSourceStream() {
        return InlineSourceDataSetProducer.super.getSrcFileSourceStream();
    }

    @Override
    default Stream<Source> getInlineSourceStream(final InlineSource inlineSource) {
        return Stream.of(inlineSource.source(this.param().addFileInfo()));
    }
}
