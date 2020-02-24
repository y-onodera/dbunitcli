package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class ColumnSettings {

    private final ColumnSetting comparisonKeys;

    private final ColumnSetting excludeColumns;

    private final ColumnSetting orderColumns;

    private final ColumnSetting expressionColumns;

    public ColumnSettings(Builder builder) {
        this.comparisonKeys = builder.comparisonKeys.build();
        this.excludeColumns = builder.excludeColumns.build();
        this.orderColumns = builder.orderColumns.build();
        this.expressionColumns = builder.expressionColumns.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnSetting getComparisonKeys() {
        return comparisonKeys;
    }

    public ColumnSetting getExcludeColumns() {
        return excludeColumns;
    }

    public ColumnSetting getOrderColumns() {
        return orderColumns;
    }

    public ColumnSetting getExpressionColumns() {
        return expressionColumns;
    }

    public Column[] getComparisonKeys(String tableName) {
        return this.getComparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    public List<Column> getExcludeColumns(String tableName) {
        return this.getExcludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    public Column[] getOrderColumns(String tableName) {
        return this.getOrderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    public ColumnExpression getExpression(String tableName) {
        return this.getExpressionColumns().getExpression(tableName);
    }

    public static class Builder {
        private ColumnSetting.Builder comparisonKeys = ColumnSetting.builder();

        private ColumnSetting.Builder excludeColumns = ColumnSetting.builder();

        private ColumnSetting.Builder orderColumns = ColumnSetting.builder();

        private ColumnSetting.Builder expressionColumns = ColumnSetting.builder();

        public ColumnSettings build(File setting) throws IOException {
            JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(setting), "MS932"));
            JsonObject settingJson = jsonReader.read()
                    .asJsonObject();
            return this.configureSetting(settingJson)
                    .configureCommonSetting(settingJson)
                    .build();
        }

        public ColumnSettings build() {
            return new ColumnSettings(this);
        }

        protected Builder configureSetting(JsonObject setting) {
            if (!setting.containsKey("settings")) {
                return this;
            }
            setting.getJsonArray("settings")
                    .forEach(v -> {
                        JsonObject json = v.asJsonObject();
                        if (json.containsKey("name")) {
                            this.addSettings(json, "name", ColumnSetting.Strategy.BY_NAME);
                        } else if (json.containsKey("pattern")) {
                            this.addSettings(json, "pattern", ColumnSetting.Strategy.PATTERN);
                        }
                    });
            return this;
        }

        protected void addSettings(JsonObject json, String name, ColumnSetting.Strategy strategy) {
            String file = json.getString(name);
            this.addComparisonKeys(strategy, json, file);
            this.addExcludeColumns(strategy, json, file);
            this.addSortColumns(strategy, json, file);
            this.addExpression(expressionColumns.getExpressionBuilder(strategy, file), json);
        }

        protected Builder configureCommonSetting(JsonObject setting) {
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
                    });
            return this;
        }

        protected void addCommonSettings(JsonObject json, String key, ColumnSetting.Builder targetSetting) {
            if (json.containsKey(key)) {
                JsonArray array = json.getJsonArray(key);
                List<String> columns = Lists.newArrayList();
                for (int i = 0, j = array.size(); i < j; i++) {
                    columns.add(array.getString(i));
                }
                targetSetting.addCommon(columns);
            }
        }

        protected void addComparisonKeys(ColumnSetting.Strategy strategy, JsonObject json, String file) {
            this.comparisonKeys.add(strategy, file, Lists.newArrayList());
            this.addSettings(strategy, json, file, "keys", this.comparisonKeys);
        }

        protected void addExcludeColumns(ColumnSetting.Strategy strategy, JsonObject json, String file) {
            this.addSettings(strategy, json, file, "exclude", this.excludeColumns);
        }

        protected void addSortColumns(ColumnSetting.Strategy strategy, JsonObject json, String file) {
            this.addSettings(strategy, json, file, "order", this.orderColumns);
        }

        protected void addSettings(ColumnSetting.Strategy strategy, JsonObject json, String file, String key, ColumnSetting.Builder comparisonKeys) {
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
        }

        protected void addExpression(ColumnExpression.Builder builder, JsonObject settingJson, ColumnExpression.ParameterType type) {
            if (settingJson.containsKey(type.keyName())) {
                JsonArray stringExpressions = settingJson.getJsonArray(type.keyName());
                for (int i = 0, j = stringExpressions.size(); i < j; i++) {
                    stringExpressions.getJsonObject(i)
                            .forEach((key, value) -> builder.addExpression(type, key, ((JsonString)value).getString()));
                }
            }
        }
    }
}
