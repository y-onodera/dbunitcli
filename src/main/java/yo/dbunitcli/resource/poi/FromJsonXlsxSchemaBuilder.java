package yo.dbunitcli.resource.poi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class FromJsonXlsxSchemaBuilder implements XlsxSchema.Builder {

    private final Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap = Maps.newHashMap();

    private final Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap = Maps.newHashMap();

    public XlsxSchema build(File schema) throws FileNotFoundException, UnsupportedEncodingException {
        if (schema == null) {
            return XlsxSchema.DEFAULT;
        }
        JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(schema), "MS932"));
        JsonObject settingJson = jsonReader.read()
                .asJsonObject();
        return this.loadRowsSettings(settingJson)
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

    protected FromJsonXlsxSchemaBuilder loadRowsSettings(JsonObject setting) {
        if (!setting.containsKey("rows")) {
            return this;
        }
        setting.getJsonArray("rows")
                .forEach(v -> this.loadRowsSetting(v.asJsonObject()));
        return this;
    }

    protected void loadRowsSetting(JsonObject jsonObject) {
        String sheetName = jsonObject.getString("sheetName");
        if (!this.rowsTableDefMap.containsKey(sheetName)) {
            this.rowsTableDefMap.put(sheetName, Lists.newArrayList());
        }
        this.rowsTableDefMap.get(sheetName).add(XlsxRowsTableDefine.builder()
                .setTableName(jsonObject.getString("tableName"))
                .setHeader(this.jsonArrayToStream(jsonObject.getJsonArray("header")))
                .setDataStartRow(jsonObject.getInt("dataStart"))
                .addCellIndexes(this.jsonArrayToIntStream(jsonObject.getJsonArray("columnIndex")))
                .addBreakKey(this.jsonArrayToStream(jsonObject.getJsonArray("breakKey")))
                .build()
        );
    }

    protected FromJsonXlsxSchemaBuilder loadCellsSettings(JsonObject setting) {
        if (!setting.containsKey("cells")) {
            return this;
        }
        setting.getJsonArray("cells")
                .forEach(v -> this.loadCellsSetting(v.asJsonObject()));
        return this;
    }

    protected void loadCellsSetting(JsonObject jsonObject) {
        String sheetName = jsonObject.getString("sheetName");
        if (!this.cellsTableDefMap.containsKey(sheetName)) {
            this.cellsTableDefMap.put(sheetName, Lists.newArrayList());
        }
        this.cellsTableDefMap.get(sheetName).add(XlsxCellsTableDefine.builder()
                .setTableName(jsonObject.getString("tableName"))
                .setHeader(this.jsonArrayToStream(jsonObject.getJsonArray("header")))
                .setRows(this.jsonArrayToStream(jsonObject.getJsonArray("rows")
                        , it -> (Integer i) -> it.getJsonObject(i).getJsonArray("cellAddress"))
                        .map(this::jsonArrayToStream))
                .build()
        );
    }

    protected Stream<Integer> jsonArrayToIntStream(JsonArray array) {
        return this.jsonArrayToStream(array, it -> it::getInt);
    }

    protected Stream<String> jsonArrayToStream(JsonArray array) {
        return this.jsonArrayToStream(array, it -> it::getString);
    }

    protected <T> Stream<T> jsonArrayToStream(JsonArray array, Function<JsonArray, Function<Integer, T>> function) {
        return Stream.iterate(0, i -> i + 1)
                .limit(array.size())
                .map(function.apply(array));
    }
}
