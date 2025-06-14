package yo.dbunitcli.application.json;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import yo.dbunitcli.Strings;
import yo.dbunitcli.common.filter.SourceFilter;
import yo.dbunitcli.resource.poi.XlsxCellsTableDefine;
import yo.dbunitcli.resource.poi.XlsxRowsTableDefine;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class FromJsonXlsxSchemaBuilder implements XlsxSchema.Builder {

    private final Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap = new HashMap<>();

    private final Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap = new HashMap<>();

    private final Map<String, SourceFilter> sheetPatterns = new HashMap<>();

    public XlsxSchema build(final File schema) throws FileNotFoundException, UnsupportedEncodingException {
        if (schema == null) {
            return XlsxSchema.DEFAULT;
        }
        return this.build(new InputStreamReader(new FileInputStream(schema), "MS932"));
    }

    public XlsxSchema build(final String json) {
        if (Strings.isEmpty(json)) {
            return XlsxSchema.DEFAULT;
        }
        return this.build(new StringReader(json));
    }

    public XlsxSchema build(final Reader reader) {
        final JsonReader jsonReader = Json.createReader(reader);
        final JsonObject settingJson = jsonReader.read()
                .asJsonObject();
        return this.loadSheetPatterns(settingJson)
                .loadRowsSettings(settingJson)
                .loadCellsSettings(settingJson)
                .build();
    }

    @Override
    public Map<String, List<XlsxRowsTableDefine>> getRowsTableDefMap() {
        return this.rowsTableDefMap;
    }

    @Override
    public Map<String, List<XlsxCellsTableDefine>> getCellsTableDefMap() {
        return this.cellsTableDefMap;
    }

    @Override
    public Map<String, SourceFilter> getSheetPatterns() {
        return this.sheetPatterns;
    }

    protected FromJsonXlsxSchemaBuilder loadSheetPatterns(final JsonObject setting) {
        if (!setting.containsKey("patterns")) {
            return this;
        }

        final JsonObject patterns = setting.getJsonObject("patterns");
        patterns.keySet().forEach(key -> {
            this.sheetPatterns.put(key, new SourceFilterParser(patterns, key).parsePattern());
        });
        return this;
    }

    protected FromJsonXlsxSchemaBuilder loadRowsSettings(final JsonObject setting) {
        if (!setting.containsKey("rows")) {
            return this;
        }
        setting.getJsonArray("rows")
                .forEach(v -> this.loadRowsSetting(v.asJsonObject()));
        return this;
    }

    protected void loadRowsSetting(final JsonObject jsonObject) {
        final String sheetName = jsonObject.getString("sheetName");
        if (!this.rowsTableDefMap.containsKey(sheetName)) {
            this.rowsTableDefMap.put(sheetName, new ArrayList<>());
        }
        this.rowsTableDefMap.get(sheetName).add(XlsxRowsTableDefine.builder()
                .setTableName(jsonObject.getString("tableName"))
                .setHeader(this.jsonArrayToStream(jsonObject.getJsonArray("header")))
                .setDataStartRow(jsonObject.getInt("dataStart"))
                .addCellIndexes(this.jsonArrayToIntStream(jsonObject.getJsonArray("columnIndex")))
                .addBreakKey(this.jsonArrayToStream(jsonObject.getJsonArray("breakKey")))
                .setAddFileInfo(jsonObject.containsKey("addFileInfo") && jsonObject.getBoolean("addFileInfo"))
                .build()
        );
    }

    protected FromJsonXlsxSchemaBuilder loadCellsSettings(final JsonObject setting) {
        if (!setting.containsKey("cells")) {
            return this;
        }
        setting.getJsonArray("cells")
                .forEach(v -> this.loadCellsSetting(v.asJsonObject()));
        return this;
    }

    protected void loadCellsSetting(final JsonObject jsonObject) {
        final String sheetName = jsonObject.getString("sheetName");
        if (!this.cellsTableDefMap.containsKey(sheetName)) {
            this.cellsTableDefMap.put(sheetName, new ArrayList<>());
        }
        this.cellsTableDefMap.get(sheetName).add(XlsxCellsTableDefine.builder()
                .setTableName(jsonObject.getString("tableName"))
                .setHeader(this.jsonArrayToStream(jsonObject.getJsonArray("header")))
                .setRows(this.jsonArrayToStream(jsonObject.getJsonArray("rows")
                                , it -> (Integer i) -> it.getJsonObject(i).getJsonArray("cellAddress"))
                        .map(this::jsonArrayToStream))
                .setAddFileInfo(jsonObject.containsKey("addFileInfo") && jsonObject.getBoolean("addFileInfo"))
                .build()
        );
    }

    protected Stream<Integer> jsonArrayToIntStream(final JsonArray array) {
        return this.jsonArrayToStream(array, it -> it::getInt);
    }

    protected Stream<String> jsonArrayToStream(final JsonArray array) {
        return this.jsonArrayToStream(array, it -> it::getString);
    }

    protected <T> Stream<T> jsonArrayToStream(final JsonArray array, final Function<JsonArray, Function<Integer, T>> function) {
        return Stream.iterate(0, i -> i + 1)
                .limit(array.size())
                .map(function.apply(array));
    }
}
