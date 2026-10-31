package yo.dbunitcli.dataset;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.common.Source;

import java.io.StringReader;
import java.util.Map;
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
     * 設定ファイルと同じ形式のJSONの設定で読み込むパラメータ
     */
    static ComparableDataSetParam param(final String setting) {
        return ComparableDataSetParam.builder().setTableSeparators(DataSetTestSupport.separators(setting)).build();
    }

    /**
     * key列にkeysの行を持つテーブルtableNameをcontextに書き込む
     */
    static void write(final ComparableTableMappingContext context, final String tableName, final String... keys) {
        new KeysTask(tableName, ComparableDataSetParam.builder().build(), (_, _) -> {
        }, keys).run(context);
    }

    /**
     * key列にkeys（既定は1〜3）の行を持つテーブルtableNameを書くソース。行を読んだか（loadDataとhasOutput）をrowsReadに記録する
     */
    static ComparableTableMappingTask recording(final String tableName, final ComparableDataSetParam param
            , final Map<String, Boolean> rowsRead, final String... keys) {
        return new KeysTask(tableName, param, (task, read) -> rowsRead.put(task.tableName(), read)
                , keys.length == 0 ? new String[]{"1", "2", "3"} : keys);
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
