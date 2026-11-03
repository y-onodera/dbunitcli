package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

/**
 * ファイルに保存していないSQL。srcのファイルの代わりに、このSQLをtableNameのテーブルとして読む（sql・csvq）
 */
public record InlineQuery(String tableName, String query) {

    public Source source(final boolean addFileInfo) {
        return new Source("", this.tableName, "", this.tableName, addFileInfo, false);
    }
}
