package yo.dbunitcli.dataset;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FromJsonTableSeparatorsBuilder extends TableSeparators.Builder {

    public TableSeparators build(final String settings) throws IOException {
        if (Optional.ofNullable(settings).orElse("").isEmpty()) {
            return this.build((File) null);
        } else {
            return this.build(Arrays.stream(settings.split(","))
                    .map(File::new)
                    .toArray(File[]::new));
        }
    }

    public TableSeparators build(final File... settings) {
        TableSeparators result = TableSeparators.NONE;
        for (final File setting : settings) {
            result = result.map(it -> it.add(this.build(setting)));
        }
        return result;
    }

    public TableSeparators build(final File setting) {
        if (setting == null) {
            return TableSeparators.NONE;
        }
        return this.load(setting).build();
    }

    public FromJsonTableSeparatorsBuilder load(final File setting) {
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

    protected FromJsonTableSeparatorsBuilder configureSetting(final JsonObject setting) {
        if (!setting.containsKey("settings")) {
            return this;
        }
        setting.getJsonArray("settings")
                .forEach(v -> {
                    final JsonObject json = v.asJsonObject();
                    if (json.containsKey("name")) {
                        this.addSettings(json, "name", TableSeparators.Strategy.BY_NAME);
                    } else if (json.containsKey("pattern")) {
                        this.addSettings(json, "pattern", TableSeparators.Strategy.PATTERN);
                    }
                });
        return this;
    }

    protected void addSettings(final JsonObject json, final String name, final TableSeparators.Strategy strategy) {
        this.addTableSeparate(strategy, json, json.getString(name));
    }

    protected FromJsonTableSeparatorsBuilder configureCommonSetting(final JsonObject setting) {
        if (setting.containsKey("commonSettings")) {
            setting.getJsonArray("commonSettings")
                    .forEach(v -> this.addCommon(this.getTableSeparator(v.asJsonObject())));
        }
        return this;
    }

    protected FromJsonTableSeparatorsBuilder importSetting(final JsonObject setting, final File file) {
        if (setting.containsKey("import")) {
            setting.getJsonArray("import").forEach(v ->
                    new FromJsonTableSeparatorsBuilder().load(new File(file.getParent(), v.asJsonObject().getString("path")))
                            .appendTo(this)
            );
        }
        return this;
    }

    protected List<String> collectSettings(final JsonObject json, final String key) {
        if (json.containsKey(key)) {
            return this.collectSettings(json.getJsonArray(key));
        }
        return new ArrayList<>();
    }

    protected List<String> collectSettings(final JsonArray keyArray) {
        return IntStream.range(0, keyArray.size())
                .mapToObj(keyArray::getString)
                .collect(Collectors.toList());
    }

    protected ExpressionColumns collectExpressionColumns(final JsonObject json) {
        return new ExpressionColumns.Builder()
                .addExpressions(this.collectExpressions(json, ExpressionColumns.ParameterType.STRING))
                .addExpressions(this.collectExpressions(json, ExpressionColumns.ParameterType.BOOLEAN))
                .addExpressions(this.collectExpressions(json, ExpressionColumns.ParameterType.NUMBER))
                .addExpressions(this.collectExpressions(json, ExpressionColumns.ParameterType.SQL_FUNCTION))
                .build();
    }

    protected ExpressionColumns.Expressions collectExpressions(final JsonObject json, final ExpressionColumns.ParameterType type) {
        final Map<String, String> result = new LinkedHashMap<>();
        if (json.containsKey(type.keyName())) {
            json.getJsonObject(type.keyName()).forEach((key, value) -> result.put(key, ((JsonString) value).getString()));
        }
        return new ExpressionColumns.Expressions(type, result);
    }

    protected List<String> collectFilters(final JsonObject settingJson) {
        if (settingJson.containsKey("filter")) {
            return this.collectSettings(settingJson, "filter");
        }
        return new ArrayList<>();
    }

    protected void appendTo(final FromJsonTableSeparatorsBuilder other) {
        other.add(this.build());
    }

    protected void addTableSeparate(final TableSeparators.Strategy strategy, final JsonObject json, final String key) {
        if (json.containsKey("separate")) {
            final JsonArray expressions = json.getJsonArray("separate");
            IntStream.range(0, expressions.size())
                    .mapToObj(expressions::getJsonObject)
                    .forEach(separate -> this.add(strategy, key, this.getTableSeparator(separate)));
        } else {
            this.add(strategy, key, this.getTableSeparator(json));
        }
    }

    protected TableSeparator getTableSeparator(final JsonObject settingJson) {
        return TableSeparator.builder()
                .setSplitter(this.getSplitter(settingJson))
                .setComparisonKeys(this.collectSettings(settingJson, "keys"))
                .setExpressionColumns(this.collectExpressionColumns(settingJson))
                .setIncludeColumns(this.collectSettings(settingJson, "include"))
                .setExcludeColumns(this.collectSettings(settingJson, "exclude"))
                .setOrderColumns(this.collectSettings(settingJson, "order"))
                .setFilter(this.collectFilters(settingJson))
                .setDistinct(this.isDistinct(settingJson))
                .build();
    }

    protected boolean isDistinct(final JsonObject settingJson) {
        if (settingJson.containsKey("distinct")) {
            return settingJson.getBoolean("distinct");
        }
        return false;
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
