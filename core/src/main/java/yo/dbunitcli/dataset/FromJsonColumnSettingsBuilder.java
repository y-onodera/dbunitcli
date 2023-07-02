package yo.dbunitcli.dataset;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FromJsonColumnSettingsBuilder implements ColumnSettings.Builder {

    private final AddSettingColumns.Builder comparisonKeys = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder excludeColumns = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder includeColumns = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder orderColumns = AddSettingColumns.NONE.builder();

    private final AddSettingColumns.Builder expressionColumns = AddSettingColumns.NONE.builder();

    private final TableSeparators.Builder separateExpressions = TableSeparators.NONE.builder();

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
    public AddSettingColumns getIncludeColumns() {
        return this.includeColumns.build();
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
    public TableSeparators getTableSeparators() {
        return this.separateExpressions.build();
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
        this.addIncludeColumns(strategy, json, key);
        this.addExcludeColumns(strategy, json, key);
        this.addSortColumns(strategy, json, key);
        this.addExpression(this.expressionColumns.getExpressionBuilder(strategy, key), json);
        this.addTableSeparate(strategy, json, key);
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
                    this.addCommonSettings(json, "include", this.includeColumns);
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
        other.includeColumns.add(this.includeColumns.build());
        other.orderColumns.add(this.orderColumns.build());
        other.expressionColumns.add(this.expressionColumns.build());
        other.separateExpressions.add(this.separateExpressions.build());
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

    protected void addIncludeColumns(final AddSettingColumns.Strategy strategy, final JsonObject json, final String file) {
        this.addSettings(strategy, json, file, "include", this.includeColumns);
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

    protected void addTableSeparate(final AddSettingColumns.Strategy strategy, final JsonObject json, final String key) {
        if (json.containsKey("separate")) {
            final JsonArray expressions = json.getJsonArray("separate");
            IntStream.range(0, expressions.size())
                    .mapToObj(expressions::getJsonObject)
                    .forEach(separate -> {
                        final JsonArray expression = separate.getJsonArray("filter");
                        this.separateExpressions.add(strategy, key,
                                new TableSeparator(this.getSplitter(separate)
                                        , IntStream.range(0, expression.size())
                                        .mapToObj(expression::getString)
                                        .collect(Collectors.toList())));
                    });
        } else {
            this.addFilterExpression(strategy, json, key);
        }
    }

    protected void addFilterExpression(final AddSettingColumns.Strategy strategy, final JsonObject settingJson, final String key) {
        if (settingJson.containsKey("filter")) {
            final JsonArray expressions = settingJson.getJsonArray("filter");
            this.separateExpressions.add(strategy, key
                    , new TableSeparator(this.getSplitter(settingJson)
                            , IntStream.range(0, expressions.size())
                            .mapToObj(expressions::getString)
                            .toList())
            );
        } else {
            this.separateExpressions.add(strategy, key, TableSeparator.NONE.with(this.getSplitter(settingJson)));
        }
    }

    protected void addFilterExpression(final JsonObject settingJson) {
        if (settingJson.containsKey("filter")) {
            final JsonArray expressions = settingJson.getJsonArray("filter");
            this.separateExpressions.addCommon(IntStream.range(0, expressions.size())
                    .mapToObj(expressions::getString)
                    .toList());
        }
    }

    protected TableSplitter getSplitter(final JsonObject json) {
        if (json.containsKey("split")) {
            final JsonObject split = json.getJsonObject("split");
            final int limit = split.getInt("limit");
            final String newName = split.containsKey("tableName") ? split.getString("tableName") : "";
            final String prefix = split.containsKey("prefix") ? split.getString("prefix") : "";
            final String suffix = split.containsKey("suffix") ? split.getString("suffix") : "";
            final List<String> breakKeys = new ArrayList<>();
            if (split.containsKey("breakKey")) {
                final JsonArray breakKey = split.getJsonArray("breakKey");
                breakKeys.addAll(IntStream.range(0, breakKey.size())
                        .mapToObj(breakKey::getString)
                        .toList());
            }
            return new TableSplitter(newName, prefix, suffix, breakKeys, limit);
        } else if (json.containsKey("tableName") || json.containsKey("prefix") || json.containsKey("suffix")) {
            final String newName = json.containsKey("tableName") ? json.getString("tableName") : "";
            final String prefix = json.containsKey("prefix") ? json.getString("prefix") : "";
            final String suffix = json.containsKey("suffix") ? json.getString("suffix") : "";
            return new TableSplitter(newName, prefix, suffix, 0);
        }
        return TableSplitter.NONE;
    }

}
