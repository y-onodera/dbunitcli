package yo.dbunitcli.dataset;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.common.Source;

import java.io.StringReader;
import java.util.stream.Stream;

/**
 * datasetのテストで使う、設定の読込とテーブルの書込
 */
final class DataSetTestSupport {

    private DataSetTestSupport() {
    }

    /**
     * 設定ファイルと同じ形式のJSON（settings・outputFilterなど）から設定を作る
     */
    static TableSeparators separators(final String setting) {
        try (final JsonReader reader = Json.createReader(new StringReader(setting))) {
            final JsonObject json = reader.readObject();
            return new FromJsonTableSeparatorsBuilder("UTF-8").configureSetting(json).configureOutputFilter(json).build();
        }
    }

    /**
     * key列にkeysの行を持つテーブルtableNameをcontextに書き込む
     */
    static void write(final ComparableTableMappingContext context, final String tableName, final String... keys) {
        final ComparableTableMapper mapper = context.createMapper(Source.NONE.tableName(tableName).createMetaData(new String[]{"key"}));
        mapper.startTable();
        Stream.of(keys).forEach(key -> mapper.addRow(new Object[]{key}));
        mapper.endTable();
    }
}
