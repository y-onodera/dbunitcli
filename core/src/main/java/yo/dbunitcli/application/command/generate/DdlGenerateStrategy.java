package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.Option;

/**
 * ddl: テーブル毎にCREATE TABLE文を生成する固定成果物型
 */
public final class DdlGenerateStrategy extends DdlMetaDataGenerateStrategy {

    @Override
    public String stgPath() {
        return "sql/ddlTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "sql/ddlTemplate.txt";
    }

    @Override
    public String defaultSettingsPath() {
        return "sql/ddlSettings.json";
    }

    @Override
    public String resultPathTemplate(final GenerateContext option) {
        return ResultPaths.sql(option);
    }

    @Override
    public void putOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        builder.put("-sqlFilePrefix", option.sqlFilePrefix()).put("-sqlFileSuffix", option.sqlFileSuffix());
    }
}
