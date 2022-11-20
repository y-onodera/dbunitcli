package yo.dbunitcli.dataset;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FromJsonColumnSettingsBuilder implements ColumnSettings.Builder {

    private final AddSettingColumns.Builder comparisonKeys = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder excludeColumns = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder orderColumns = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder expressionColumns = AddSettingColumns.NONE.builder();

    private final RowFilters.Builder filterExpressions = RowFilters.NONE.builder();

    private final TableSplitter.Builder tableSplitters = TableSplitter.NONE.builder();

    @Override
    public ColumnSettings build(final File setting) {
        if (setting == null) {
            return ColumnSettings.NONE;
        }
        return this.load(setting).build();
    }

    public FromJsonColumnSettingsBuilder load(final File setting) {
        try {
            final JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(setting), "MS932"));
            final JsonObject settingJson = jsonReader.read()
                    .asJsonObject();
            return this.configureSetting(settingJson)
                    .configureCommonSetting(settingJson)
                    .importSetting(settingJson, setting);
        } catch (final UnsupportedEncodingException | FileNotFoundException e) {
            throw new AssertionError(e);
        }
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
    public RowFilters getRowFilters() {
        return this.filterExpressions.build();
    }

    @Override
    public TableSplitter getTableSplitters() {
        return this.tableSplitters.build();
    }

    protected FromJsonColumnSettingsBuilder configureSetting(final JsonObject setting) {
        if (!setting.containsKey("settings")) {
            return this;
        }
        setting.getJsonArray("settings")
                .forEach(v -> {
                    final JsonObject json = v.asJsonObject();
                    if (json.containsKey("name")) {
                        this.addSettings(json, "name", AddSettingColumns.Strategy.BY_NAME);
                    } else if (json.containsKey("pattern")) {
                        this.addSettings(json, "pattern", AddSettingColumns.Strategy.PATTERN);
                    }
                });
        return this;
    }

    protected void addSettings(final JsonObject json, final String name, final AddSettingColumns.Strategy strategy) {
        final String key = json.getString(name);
        this.addComparisonKeys(strategy, json, key);
        this.addExcludeColumns(strategy, json, key);
        this.addSortColumns(strategy, json, key);
        this.addExpression(this.expressionColumns.getExpressionBuilder(strategy, key), json);
        this.addTableSplitter(strategy, json, key);
    }

    protected FromJsonColumnSettingsBuilder configureCommonSetting(final JsonObject setting) {
        if (!setting.containsKey("commonSettings")) {
            return this;
        }
        setting.getJsonArray("commonSettings")
                .forEach(v -> {
                    final JsonObject json = v.asJsonObject();
                    this.addCommonSettings(json, "keys", this.comparisonKeys);
                    this.addCommonSettings(json, "exclude", this.excludeColumns);
                    this.addCommonSettings(json, "order", this.orderColumns);
                    this.addExpression(this.expressionColumns.getCommonExpressionBuilder(), json);
                    this.addFilterExpression(json);
                });
        return this;
    }

    protected FromJsonColumnSettingsBuilder importSetting(final JsonObject setting, final File file) {
        if (!setting.containsKey("import")) {
            return this;
        }
        setting.getJsonArray("import").forEach(v ->
                new FromJsonColumnSettingsBuilder().load(new File(file.getParent(), v.asJsonObject().getString("path")))
                        .appendTo(this)
        );
        return this;
    }

    protected void appendTo(final FromJsonColumnSettingsBuilder other) {
        other.comparisonKeys.add(this.comparisonKeys.build());
        other.excludeColumns.add(this.excludeColumns.build());
        other.orderColumns.add(this.orderColumns.build());
        other.expressionColumns.add(this.expressionColumns.build());
        other.filterExpressions.add(this.filterExpressions.build());
    }

    protected void addCommonSettings(final JsonObject json, final String key, final AddSettingColumns.Builder targetSetting) {
        if (json.containsKey(key)) {
            final JsonArray array = json.getJsonArray(key);
            targetSetting.addCommon(IntStream.range(0, array.size())
                    .mapToObj(array::getString)
                    .collect(Collectors.toList()));
        }
    }

    protected void addComparisonKeys(final AddSettingColumns.Strategy strategy, final JsonObject json, final String file) {
        this.comparisonKeys.add(strategy, file, new ArrayList<>());
        this.addSettings(strategy, json, file, "keys", this.comparisonKeys);
    }

    protected void addExcludeColumns(final AddSettingColumns.Strategy strategy, final JsonObject json, final String file) {
        this.addSettings(strategy, json, file, "exclude", this.excludeColumns);
    }

    protected void addSortColumns(final AddSettingColumns.Strategy strategy, final JsonObject json, final String file) {
        this.addSettings(strategy, json, file, "order", this.orderColumns);
    }

    protected void addSettings(final AddSettingColumns.Strategy strategy, final JsonObject json, final String file, final String key, final AddSettingColumns.Builder comparisonKeys) {
        if (json.containsKey(key)) {
            final JsonArray keyArray = json.getJsonArray(key);
            comparisonKeys.add(strategy, file, IntStream.range(0, keyArray.size())
                    .mapToObj(keyArray::getString)
                    .collect(Collectors.toList()));
        }
    }

    protected void addExpression(final ColumnExpression.Builder builder, final JsonObject json) {
        this.addExpression(builder, json, ColumnExpression.ParameterType.STRING);
        this.addExpression(builder, json, ColumnExpression.ParameterType.BOOLEAN);
        this.addExpression(builder, json, ColumnExpression.ParameterType.NUMBER);
        this.addExpression(builder, json, ColumnExpression.ParameterType.SQL_FUNCTION);
    }

    protected void addExpression(final ColumnExpression.Builder builder, final JsonObject settingJson, final ColumnExpression.ParameterType type) {
        if (settingJson.containsKey(type.keyName())) {
            settingJson.getJsonObject(type.keyName()).forEach((key, value)
                    -> builder.addExpression(type, key, ((JsonString) value).getString()));
        }
    }

    protected void addTableSplitter(final AddSettingColumns.Strategy strategy, final JsonObject json, final String key) {
        if (json.containsKey("split")) {
            final JsonArray expressions = json.getJsonArray("split");
            IntStream.range(0, expressions.size())
                    .mapToObj(expressions::getJsonObject)
                    .forEach(splitDefine -> {
                        final JsonArray filters = splitDefine.getJsonArray("filter");
                        final String resultName = splitDefine.getString("resultName");
                        final List<String> filterExpressions = IntStream.range(0, filters.size())
                                .mapToObj(filters::getString)
                                .collect(Collectors.toList());
                        if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                            this.tableSplitters.addByNames(key, filterExpressions, resultName);
                        } else {
                            this.tableSplitters.addPattern(key, filterExpressions, resultName);
                        }
                    });
        } else {
            this.addFilterExpression(strategy, json, key);
        }
    }

    protected void addFilterExpression(final AddSettingColumns.Strategy strategy, final JsonObject settingJson, final String key) {
        if (settingJson.containsKey("filter")) {
            final JsonArray expressions = settingJson.getJsonArray("filter");
            IntStream.range(0, expressions.size())
                    .forEach(i -> this.filterExpressions.add(strategy, key, expressions.getString(i)));
        }
        this.filterExpressions.addRenameFunction(strategy, key, this.toTableNameMapFunction(strategy, settingJson));
    }

    protected void addFilterExpression(final JsonObject settingJson) {
        if (settingJson.containsKey("filter")) {
            final JsonArray expressions = settingJson.getJsonArray("filter");
            IntStream.range(0, expressions.size())
                    .forEach(i -> this.filterExpressions.addCommon(expressions.getString(i)));
        }
    }

    protected Function<String, String> toTableNameMapFunction(final AddSettingColumns.Strategy strategy, final JsonObject json) {
        if (!json.containsKey("tableName")) {
            return Function.identity();
        }
        final String result = json.getString("tableName");
        if (strategy == AddSettingColumns.Strategy.BY_NAME) {
            final String name = json.getString("name");
            return it -> it.equals(name) ? result : it;
        } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
            final String pattern = json.getString("pattern");
            return it -> AddSettingColumns.ALL_MATCH_PATTERN.equals(pattern) || it.contains(pattern) ? result : it;
        }
        return Function.identity();
    }

}
