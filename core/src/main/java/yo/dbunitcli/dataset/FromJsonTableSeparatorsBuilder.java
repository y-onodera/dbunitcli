package yo.dbunitcli.dataset;

import javax.json.*;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;
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
            final JsonObject settingJson = jsonReader.read().asJsonObject();
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
                .forEach(v -> this.addSettings(v.asJsonObject()));
        return this;
    }

    protected void addSettings(final JsonObject json) {
        this.addTableSeparate(json, this.getTargetFilter(json));
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

    protected void addTableSeparate(final JsonObject json, final Predicate<String> targetFilter) {
        if (json.containsKey("separate")) {
            final JsonArray expressions = json.getJsonArray("separate");
            IntStream.range(0, expressions.size())
                    .mapToObj(expressions::getJsonObject)
                    .forEach(separate -> this.addSetting(this.getTableSeparator(separate, targetFilter)));
        } else if (json.containsKey("innerJoin")) {
            this.addJoin(this.getJoin(json.getJsonObject("innerJoin")
                    , ComparableTableJoin.innerJoin(this.getJoinOn(json.getJsonObject("innerJoin")))
                    , this.getTableSeparator(json, targetFilter)));
        } else if (json.containsKey("outerJoin")) {
            this.addJoin(this.getJoin(json.getJsonObject("outerJoin")
                    , ComparableTableJoin.outerJoin(this.getJoinOn(json.getJsonObject("outerJoin")))
                    , this.getTableSeparator(json, targetFilter)));
        } else if (json.containsKey("fullJoin")) {
            this.addJoin(this.getJoin(json.getJsonObject("fullJoin")
                    , ComparableTableJoin.fullJoin(this.getJoinOn(json.getJsonObject("fullJoin")))
                    , this.getTableSeparator(json, targetFilter)));
        } else {
            this.addSetting(this.getTableSeparator(json, targetFilter));
        }
    }

    protected TableSeparator getTableSeparator(final JsonObject settingJson) {
        return this.getTableSeparator(settingJson, this.getTargetFilter(settingJson));
    }

    protected TableSeparator getTableSeparator(final JsonObject settingJson, final Predicate<String> targetFilter) {
        return TableSeparator.builder()
                .setTargetFilter(targetFilter)
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

    protected Predicate<String> getTargetFilter(final JsonObject settingJson) {
        if (settingJson.containsKey("name")) {
            if (settingJson.get("name") instanceof JsonString targetName) {
                return (it) -> it.equals(targetName.getString());
            } else {
                final JsonArray names = settingJson.getJsonArray("name");
                final List<String> nameList = IntStream.range(0, names.size())
                        .mapToObj(names::getString)
                        .toList();
                return nameList::contains;
            }
        } else if (settingJson.containsKey("pattern")) {
            final String targetPattern = settingJson.getString("pattern");
            return (it) -> it.contains(targetPattern) || targetPattern.equals("*");
        } else if (settingJson.containsKey("innerJoin")
                || settingJson.containsKey("outerJoin")
                || settingJson.containsKey("fullJoin")) {
            return TableSeparator.REJECT_ALL;
        }
        return TableSeparator.ACCEPT_ALL;
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

    protected JoinCondition getJoin(final JsonObject json, final ComparableTableJoin.Strategy strategy, final TableSeparator tableSeparator) {
        return JoinCondition.builder()
                .setLeft(json.getString("left"))
                .setRight(json.getString("right"))
                .setStrategy(strategy)
                .setTableSeparator(tableSeparator)
                .build();
    }

    protected ComparableTableJoin.ConditionBuilder getJoinOn(final JsonObject json) {
        if (json.containsKey("column")) {
            final JsonArray columns = json.getJsonArray("column");
            return ComparableTableJoin.equals(IntStream.range(0, columns.size())
                    .mapToObj(columns::getString)
                    .collect(Collectors.toSet()));
        }
        return ComparableTableJoin.eval(json.getString("on"));
    }

}
