package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.InlineSource;

import java.util.stream.Stream;

/**
 * srcのファイルの代わりに、未保存の内容（{@link yo.dbunitcli.dataset.ComparableDataSetParam#inlineSource()}）を読めるproducer
 * （{@link DataSourceType#supportsInlineSource()}の形式）。inlineSourceがあれば、srcのファイルは読まず、
 * {@link #getSrc()}は空、ファイル情報の列（addFileInfo）のパスは空になる
 */
public interface InlineSourceDataSetProducer extends ComparableDataSetProducer {

    @Override
    default Stream<? extends Source> getSourceStream() {
        final InlineSource inlineSource = this.param().inlineSource();
        if (inlineSource == null) {
            return this.getSrcFileSourceStream();
        }
        return this.getInlineSourceStream(inlineSource)
                   .filter(it -> this.param().tableNameFilter().predicate(it.getTableName()));
    }

    @Override
    default String getSrc() {
        return this.param().inlineSource() == null ? ComparableDataSetProducer.super.getSrc() : "";
    }

    /**
     * srcのファイルから読むソース（テーブル名での絞り込みを含む）
     */
    default Stream<? extends Source> getSrcFileSourceStream() {
        return ComparableDataSetProducer.super.getSourceStream();
    }

    /**
     * inlineSourceの内容から読むソース。テーブル名での絞り込みは呼び出し側で行う
     */
    Stream<Source> getInlineSourceStream(InlineSource inlineSource);
}
