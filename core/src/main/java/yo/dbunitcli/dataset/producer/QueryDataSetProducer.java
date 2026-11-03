package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.InlineQuery;

import java.io.File;
import java.util.stream.Stream;

public interface QueryDataSetProducer extends ComparableDataSetProducer {

    /**
     * sourceのSQLをテンプレートとして描画する。inlineQueryがあればsrcのファイルの代わりにそのSQLを使う
     */
    static String renderQuery(final ComparableDataSetParam param, final Source source, final Parameter parameter) {
        final InlineQuery inlineQuery = param.inlineQuery();
        return inlineQuery != null
                ? param.templateRender().render(inlineQuery.query(), parameter)
                : param.templateRender().render(new File(source.filePath()), parameter);
    }

    Parameter parameter();

    @Override
    default Stream<? extends Source> getSourceStream() {
        final InlineQuery inlineQuery = this.param().inlineQuery();
        if (inlineQuery == null) {
            return ComparableDataSetProducer.super.getSourceStream();
        }
        return Stream.of(inlineQuery.source(this.param().addFileInfo()))
                     .filter(it -> this.param().tableNameFilter().predicate(it.getTableName()));
    }
}
