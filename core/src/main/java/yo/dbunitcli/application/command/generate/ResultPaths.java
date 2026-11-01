package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.command.GenerateOption;

/**
 * テーブル毎に1ファイル出力する型の-resultPathパターン
 */
final class ResultPaths {

    private ResultPaths() {
    }

    static String tableFile(final GenerateOption option) {
        return ResultPaths.tableFile(option, "", "", ".json", null);
    }

    static String tableFile(final GenerateOption option, final String prefix, final String suffix,
                            final String extension, final String nameFormat) {
        final String tableName = nameFormat == null
                ? option.templateOption().getTemplateRender().getAttributeName("tableName")
                : option.templateOption().getTemplateRender().getAttributeName("tableName", nameFormat);
        return option.resultPath() + "/" + prefix + tableName + suffix + extension;
    }

    static String sql(final GenerateOption option) {
        return ResultPaths.tableFile(option, option.sqlFilePrefix(), option.sqlFileSuffix(), ".sql", null);
    }
}
