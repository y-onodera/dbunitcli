package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComparableTableMappingTaskTest {

    private final List<String> runs = new ArrayList<>();

    @Test
    public void 行を流して分割するテーブルに複数のソースから行が入ると_ソースをまたいで分割しソースは1回だけ読む() {
        final Map<String, List<String>> rows = this.run("""
                {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}]}
                """, true);

        Assertions.assertEquals(Map.of("a_00", List.of("11", "12"), "a_01", List.of("13", "21"), "a_02", List.of("22", "23")), rows);
        Assertions.assertEquals(List.of("11", "21"), this.runs);
    }

    @Test
    public void 分割できないconverterでseparateしても_ソースは1回だけ読み_それぞれのテーブルにそのテーブルの行だけを書く() {
        final Map<String, List<String>> rows = this.run("""
                {"settings": [{"name": "a", "separate": [{"tableName": "x", "filter": ["key != '12'"]}, {"tableName": "y"}]}]}
                """, false);

        Assertions.assertEquals(List.of("11", "13", "21", "22", "23"), rows.get("x"));
        Assertions.assertEquals(List.of("11", "12", "13", "21", "22", "23"), rows.get("y"));
        Assertions.assertEquals(List.of("11", "21"), this.runs);
    }

    /**
     * 2つのソース（11〜13・21〜23）がaに書く行を、jxlsと同じく行を流して受け取るconverterで読み、テーブルごとの行を返す
     */
    private Map<String, List<String>> run(final String setting, final boolean splittable) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(DataSetTestSupport.separators(setting))
                                                                   .build();
        final Map<String, List<String>> rows = new LinkedHashMap<>();
        new ComparableTableMappingTask.WithTargetTable("a", List.of(new KeysTask(param, this.runs, "11", "12", "13")
                , new KeysTask(param, this.runs, "21", "22", "23")))
                .runWith(_ -> {
                }, new RowsByTable(rows, new String[1], splittable));
        return rows;
    }

    /**
     * テーブルaにkeysの行を書くソース。読んだことをrunsに1つ目のkeyで記録する
     */
    private record KeysTask(ComparableDataSetParam param, List<String> runs, String... keys) implements ComparableTableMappingTask {

        @Override
        public Source source() {
            return Source.NONE.tableName("a");
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            this.runs.add(this.keys[0]);
            DataSetTestSupport.write(context, "a", this.keys);
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new KeysTask(builder.build(), this.runs, this.keys);
        }
    }

    /**
     * jxls（StreamingEachCommand）と同じく行を流して受け取り、テーブルごとの行を記録する。
     * 分割（split）しても書き込み先と書き込み中のテーブル（current）を共有する。splittableがfalseならxlsのシート出力と同じく分割できない
     */
    private record RowsByTable(Map<String, List<String>> rows, String[] current, boolean splittable) implements IDataSetConverter {

        @Override
        public boolean isExportEmptyTable() {
            return false;
        }

        @Override
        public boolean isSplittable() {
            return this.splittable;
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            this.startTable(tableMetaData);
        }

        @Override
        public IDataSetConverter split() {
            return new RowsByTable(this.rows, this.current, this.splittable);
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.current[0] = metaData.getTableName();
        }

        @Override
        public void endTable() {
            this.current[0] = null;
        }

        @Override
        public void row(final Object[] values) {
            this.rows.computeIfAbsent(this.current[0], _ -> new ArrayList<>()).add(values[0].toString());
        }
    }
}
