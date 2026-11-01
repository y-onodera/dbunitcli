package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.GenerateOption;

import java.util.List;

/**
 * settings: データセットのメタデータからdataset設定ファイルを生成する固定成果物型
 */
public final class SettingsGenerateStrategy implements GenerateStrategy {

    @Override
    public String stgPath() {
        return "settings/settingTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "settings/settingTemplate.txt";
    }

    @Override
    public boolean isFixedTemplate() {
        return true;
    }

    @Override
    public ParameterUnit fixedUnit() {
        return ParameterUnit.dataset;
    }

    @Override
    public boolean loadData() {
        return false;
    }

    @Override
    public boolean useJdbcMetaData() {
        return true;
    }

    @Override
    public List<Option.Violation> validateOption(final GenerateOption option) {
        return option.validateBoolean("-includeAllColumns", option.includeAllColumns());
    }

    @Override
    public void putOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
        builder.put("-includeAllColumns", option.includeAllColumns());
    }
}
