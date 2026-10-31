package yo.dbunitcli.dataset;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.common.Source;

import java.io.StringReader;
import java.util.function.BiConsumer;
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

    /**
     * key列にkeysの行を持つテーブルtableNameを書くソース。実行するたびに、行を読んだか（loadDataとhasOutput）をonRunへ通知する
     */
    record KeysTask(String tableName, ComparableDataSetParam param, BiConsumer<KeysTask, Boolean> onRun
            , String... keys) implements ComparableTableMappingTask {

        @Override
        public Source source() {
            return Source.NONE.tableName(this.tableName);
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            final ComparableTableMapper mapper = context.createMapper(this.source().createMetaData(new String[]{"key"}));
            mapper.startTable();
            final boolean read = this.param.loadData() && mapper.hasOutput();
            this.onRun.accept(this, read);
            if (read) {
                Stream.of(this.keys).forEach(key -> mapper.addRow(new Object[]{key}));
            }
            mapper.endTable();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new KeysTask(this.tableName, builder.build(), this.onRun, this.keys);
        }
    }
}
