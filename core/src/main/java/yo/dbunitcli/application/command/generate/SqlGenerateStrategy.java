package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.resource.FileResources;

import java.util.List;

/**
 * sql: テーブル毎に-opに応じたDML（insert/delete/update等）を生成する固定成果物型
 */
public final class SqlGenerateStrategy implements GenerateStrategy {

    @Override
    public String stgPath() {
        return "sql/sqlTemplate.stg";
    }

    @Override
    public String templateString(final GenerateOption option) {
        return FileResources.readClasspathResource(switch (option.operation()) {
            case INSERT -> "sql/insertTemplate.txt";
            case DELETE -> "sql/deleteTemplate.txt";
            case UPDATE -> "sql/updateTemplate.txt";
            case CLEAN_INSERT -> "sql/cleanInsertTemplate.txt";
            default -> "sql/deleteInsertTemplate.txt";
        });
    }

    @Override
    public boolean isFixedTemplate() {
        return true;
    }

    @Override
    public ParameterUnit fixedUnit() {
        return ParameterUnit.table;
    }

    @Override
    public boolean useJdbcMetaData() {
        return true;
    }

    @Override
    public String resultPathTemplate(final GenerateOption option) {
        return ResultPaths.sql(option);
    }

    @Override
    public List<Option.Violation> validateOption(final GenerateOption option) {
        return Option.concat(
                option.validateRequired("-op", option.operation() == null ? null : option.operation().name())
                , option.validateBoolean("-commit", option.commit()));
    }

    @Override
    public void putOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
        builder.put("-commit", option.commit()).put("-op", option.operation(), DbOperation.class)
               .put("-sqlFilePrefix", option.sqlFilePrefix()).put("-sqlFileSuffix", option.sqlFileSuffix());
    }
}
