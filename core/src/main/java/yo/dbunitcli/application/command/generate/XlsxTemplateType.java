package yo.dbunitcli.application.command.generate;

/**
 * xlsxTemplateの-templateTypeの値
 */
public enum XlsxTemplateType {
    /** テーブルごとに1シート */
    table,
    /** データセットの2テーブルのデータシートと、両者を突合する比較シート */
    compare,
    /** unit=tableで-src.loadMetaDataの記述子行を流し込むテーブル定義書（レイアウトはsrcに依らない） */
    metadata
}
