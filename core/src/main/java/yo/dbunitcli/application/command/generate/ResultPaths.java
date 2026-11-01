package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.resource.st4.TemplateRender;

/**
 * テーブル毎に1ファイル出力する型の-resultPathパターン
 */
final class ResultPaths {

    private ResultPaths() {
    }

    /** テーブル毎のファイル名のパターン（tableNameをテンプレート式で埋め込む。.paramの-resultPathにもそのまま使える） */
    static String fileName(final TemplateRender render, final String prefix, final String suffix,
                           final String extension, final String nameFormat) {
        final String tableName = nameFormat == null
                ? render.getAttributeExpression("tableName")
                : render.getAttributeName("tableName", nameFormat);
        return prefix + tableName + suffix + extension;
    }

    static String tableFile(final GenerateContext option, final String prefix, final String suffix,
                            final String extension, final String nameFormat) {
        return option.resultPath() + "/"
                + ResultPaths.fileName(option.templateOption().getTemplateRender(), prefix, suffix, extension, nameFormat);
    }

    static String sql(final GenerateContext option) {
        return ResultPaths.tableFile(option, option.sqlFilePrefix(), option.sqlFileSuffix(), ".sql", null);
    }
}
