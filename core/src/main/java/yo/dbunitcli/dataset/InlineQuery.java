package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

/**
 * ファイルに保存していないsrcの内容。srcのファイルの代わりに読む。
 * sql・csvqはSQLをtableNameのテーブルとして、tableは1行1テーブル名の一覧として読む（tableNameはファイル名の代わり）
 */
public record InlineQuery(String tableName, String query) {

    public Source source(final boolean addFileInfo) {
        return new Source("", this.tableName, "", this.tableName, addFileInfo, false);
    }
}
