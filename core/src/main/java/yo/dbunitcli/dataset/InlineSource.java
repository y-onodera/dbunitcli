package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

/**
 * ファイルに保存していないsrcの内容。{@link DataSourceType#supportsInlineSource()}の形式だけが、srcのファイルの代わりに読む。
 * contentの読み方は{@link yo.dbunitcli.dataset.producer.InlineSourceDataSetProducer}の各実装、tableNameはsrcのファイル名の代わり
 */
public record InlineSource(String tableName, String content) {

    public Source source(final boolean addFileInfo) {
        return new Source("", this.tableName, "", this.tableName, addFileInfo, false);
    }
}
