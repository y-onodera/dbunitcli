package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.List;

public class ColumnSettings {

    private final ColumnSetting comparisonKeys;

    private final ColumnSetting excludeColumns;

    private final ColumnSetting orderColumns;

    public ColumnSettings(Builder builder) {
        this.comparisonKeys = builder.comparisonKeys.build();
        this.excludeColumns = builder.excludeColumns.build();
        this.orderColumns = builder.orderColumns.build();
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

    public static class Builder {
        private ColumnSetting.Builder comparisonKeys = ColumnSetting.builder();

        private ColumnSetting.Builder excludeColumns = ColumnSetting.builder();

        private ColumnSetting.Builder orderColumns = ColumnSetting.builder();

        public ColumnSettings build(File setting) throws IOException {
            JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(setting), "windows-31j"));
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
                            String file = json.getString("name");
                            ColumnSetting.Strategy strategy = ColumnSetting.Strategy.BY_NAME;
                            this.addComparisonKeys(strategy, json, file);
                            this.addExcludeColumns(strategy, json, file);
                            this.addSortColumns(strategy, json, file);
                        } else if (json.containsKey("pattern")) {
                            String file = json.getString("pattern");
                            ColumnSetting.Strategy strategy = ColumnSetting.Strategy.PATTERN;
                            this.addComparisonKeys(strategy, json, file);
                            this.addExcludeColumns(strategy, json, file);
                            this.addSortColumns(strategy, json, file);
                        }
                    });
            return this;
        }

        protected Builder configureCommonSetting(JsonObject setting) {
            if (!setting.containsKey("commonSettings")) {
                return this;
            }
            setting.getJsonArray("commonSettings")
                    .forEach(v -> {
                        JsonObject json = v.asJsonObject();
                        addCommonSettings(json, "keys", this.comparisonKeys);
                        addCommonSettings(json, "exclude", this.excludeColumns);
                        addCommonSettings(json, "order", this.orderColumns);
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
    }
}
