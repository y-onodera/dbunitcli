package yo.dbunitcli.application.setting;

import com.google.common.collect.Lists;
import yo.dbunitcli.dataset.ColumnExpression;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ColumnSettings;
import yo.dbunitcli.dataset.RowFilter;

import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class FromJsonColumnSettingsBuilder implements ColumnSettings.Builder {

    private AddSettingColumns.Builder comparisonKeys = AddSettingColumns.builder();

    private AddSettingColumns.Builder excludeColumns = AddSettingColumns.builder();

    private AddSettingColumns.Builder orderColumns = AddSettingColumns.builder();

    private AddSettingColumns.Builder expressionColumns = AddSettingColumns.builder();

    private RowFilter.Builder filterExpressions = RowFilter.builder();

    @Override
    public ColumnSettings build(File setting) throws IOException {
        if (setting == null) {
            return ColumnSettings.NONE;
        }
        JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(setting), "MS932"));
        JsonObject settingJson = jsonReader.read()
                .asJsonObject();
        return this.configureSetting(settingJson)
                .configureCommonSetting(settingJson)
                .build();
    }

    @Override
    public AddSettingColumns getComparisonKeys() {
        return this.comparisonKeys.build();
    }

    @Override
    public AddSettingColumns getExcludeColumns() {
        return this.excludeColumns.build();
    }

    @Override
    public AddSettingColumns getOrderColumns() {
        return this.orderColumns.build();
    }

    @Override
    public AddSettingColumns getExpressionColumns() {
        return this.expressionColumns.build();
    }

    @Override
    public RowFilter getFilterExpressions() {
        return this.filterExpressions.build();
    }

    protected FromJsonColumnSettingsBuilder configureSetting(JsonObject setting) {
        if (!setting.containsKey("settings")) {
            return this;
        }
        setting.getJsonArray("settings")
                .forEach(v -> {
                    JsonObject json = v.asJsonObject();
                    if (json.containsKey("name")) {
                        this.addSettings(json, "name", AddSettingColumns.Strategy.BY_NAME);
                    } else if (json.containsKey("pattern")) {
                        this.addSettings(json, "pattern", AddSettingColumns.Strategy.PATTERN);
                    }
                });
        return this;
    }

    protected void addSettings(JsonObject json, String name, AddSettingColumns.Strategy strategy) {
        String key = json.getString(name);
        this.addComparisonKeys(strategy, json, key);
        this.addExcludeColumns(strategy, json, key);
        this.addSortColumns(strategy, json, key);
        this.addExpression(this.expressionColumns.getExpressionBuilder(strategy, key), json);
        this.addFilterExpression(strategy, json, key);
    }

    protected ColumnSettings.Builder configureCommonSetting(JsonObject setting) {
        if (!setting.containsKey("commonSettings")) {
            return this;
        }
        setting.getJsonArray("commonSettings")
                .forEach(v -> {
                    JsonObject json = v.asJsonObject();
                    this.addCommonSettings(json, "keys", this.comparisonKeys);
                    this.addCommonSettings(json, "exclude", this.excludeColumns);
                    this.addCommonSettings(json, "order", this.orderColumns);
                    this.addExpression(this.expressionColumns.getCommonExpressionBuilder(), json);
                    this.addFilterExpression(json);
                });
        return this;
    }

    protected void addCommonSettings(JsonObject json, String key, AddSettingColumns.Builder targetSetting) {
        if (json.containsKey(key)) {
            JsonArray array = json.getJsonArray(key);
            List<String> columns = Lists.newArrayList();
            for (int i = 0, j = array.size(); i < j; i++) {
                columns.add(array.getString(i));
            }
            targetSetting.addCommon(columns);
        }
    }

    protected void addComparisonKeys(AddSettingColumns.Strategy strategy, JsonObject json, String file) {
        this.comparisonKeys.add(strategy, file, Lists.newArrayList());
        this.addSettings(strategy, json, file, "keys", this.comparisonKeys);
    }

    protected void addExcludeColumns(AddSettingColumns.Strategy strategy, JsonObject json, String file) {
        this.addSettings(strategy, json, file, "exclude", this.excludeColumns);
    }

    protected void addSortColumns(AddSettingColumns.Strategy strategy, JsonObject json, String file) {
        this.addSettings(strategy, json, file, "order", this.orderColumns);
    }

    protected void addSettings(AddSettingColumns.Strategy strategy, JsonObject json, String file, String key, AddSettingColumns.Builder comparisonKeys) {
        if (json.containsKey(key)) {
            JsonArray keyArray = json.getJsonArray(key);
            List<String> keys = Lists.newArrayList();
            for (int i = 0, j = keyArray.size(); i < j; i++) {
                keys.add(keyArray.getString(i));
            }
            comparisonKeys.add(strategy, file, keys);
        }
    }

    protected void addExpression(ColumnExpression.Builder builder, JsonObject json) {
        this.addExpression(builder, json, ColumnExpression.ParameterType.STRING);
        this.addExpression(builder, json, ColumnExpression.ParameterType.BOOLEAN);
        this.addExpression(builder, json, ColumnExpression.ParameterType.NUMBER);
        this.addExpression(builder, json, ColumnExpression.ParameterType.SQL_FUNCTION);
    }

    protected void addExpression(ColumnExpression.Builder builder, JsonObject settingJson, ColumnExpression.ParameterType type) {
        if (settingJson.containsKey(type.keyName())) {
            this.addExpression(builder, settingJson.getJsonArray(type.keyName()), type);
        }
    }

    protected void addExpression(ColumnExpression.Builder builder, JsonArray expressions, ColumnExpression.ParameterType aType) {
        for (int i = 0, j = expressions.size(); i < j; i++) {
            expressions.getJsonObject(i)
                    .forEach((key, value) -> builder.addExpression(aType, key, ((JsonString) value).getString()));
        }
    }

    protected void addFilterExpression(AddSettingColumns.Strategy strategy, JsonObject settingJson, String key) {
        if (settingJson.containsKey("filter")) {
            JsonArray expressions = settingJson.getJsonArray("filter");
            for (int i = 0, j = expressions.size(); i < j; i++) {
                this.filterExpressions.add(strategy, key, expressions.getString(i));
            }
        }
    }

    protected void addFilterExpression(JsonObject settingJson) {
        if (settingJson.containsKey("filter")) {
            JsonArray expressions = settingJson.getJsonArray("filter");
            for (int i = 0, j = expressions.size(); i < j; i++) {
                this.filterExpressions.addCommon(expressions.getString(i));
            }
        }
    }

}
