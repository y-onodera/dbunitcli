package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ComparableTableMappingTaskTest {

    private final List<String> runs = new ArrayList<>();

    @Test
    public void 行を流して分割するテーブルに複数のソースから行が入ると_ソースをまたいで分割しソースは1回だけ読む() {
        final String setting = """
                {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}]}
                """;
        Assertions.assertEquals(List.of("11", "12"), this.streamedRows(setting, "a_00"));
        Assertions.assertEquals(List.of("13", "21"), this.streamedRows(setting, "a_01"));
        Assertions.assertEquals(List.of("22", "23"), this.streamedRows(setting, "a_02"));
        Assertions.assertEquals(List.of("11", "21", "11", "21", "11", "21"), this.runs);
    }

    @Test
    public void 分割できないconverterでseparateしても_ソースは1回だけ読み_それぞれのテーブルにそのテーブルの行だけを書く() {
        final TableSeparators separators = DataSetTestSupport.separators("""
                {"settings": [{"name": "a", "separate": [{"tableName": "x", "filter": ["key != '12'"]}, {"tableName": "y"}]}]}
                """);
        final Map<String, List<String>> rows = new LinkedHashMap<>();
        final ComparableTableMappingContext context = new ComparableTableMappingContext(separators, new RowsByTable(rows));
        context.open();
        this.task(separators, "x").run(context);
        context.close();

        Assertions.assertEquals(List.of("11", "13", "21", "22", "23"), rows.get("x"));
        Assertions.assertEquals(List.of("11", "12", "13", "21", "22", "23"), rows.get("y"));
        Assertions.assertEquals(List.of("11", "21"), this.runs);
    }

    /**
     * 2つのソース（11〜13・21〜23）がaに書いた行を、jxlsと同じく対象のテーブルの行だけを受け取るconverterで読む
     */
    private List<String> streamedRows(final String setting, final String targetTableName) {
        final TableSeparators separators = DataSetTestSupport.separators(setting);
        final List<String> rows = new ArrayList<>();
        final ComparableTableMappingContext context = new ComparableTableMappingContext(separators, new TargetTableRows(targetTableName, rows, new String[1]));
        context.open();
        this.task(separators, targetTableName).run(context);
        context.close();
        return rows;
    }

    private ComparableTableMappingTask.WithTargetTable task(final TableSeparators separators, final String targetTableName) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder().setTableSeparators(separators).build();
        return new ComparableTableMappingTask.WithTargetTable(targetTableName
                , new KeysTask(param, this.runs, "11", "12", "13")
                , List.of(new KeysTask(param, this.runs, "21", "22", "23")));
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
            final ComparableTableMapper mapper = context.createMapper(this.source().createMetaData(new String[]{"key"}));
            mapper.startTable();
            Stream.of(this.keys).forEach(key -> mapper.addRow(new Object[]{key}));
            mapper.endTable();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new KeysTask(builder.build(), this.runs, this.keys);
        }
    }

    /**
     * jxls（StreamingEachCommand）と同じく、対象のテーブルの行だけを受け取る。分割（split）しても書き込み先を共有する
     */
    private record TargetTableRows(String targetTableName, List<String> rows, String[] current) implements IDataSetConverter {

        @Override
        public boolean isExportEmptyTable() {
            return false;
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            this.startTable(tableMetaData);
        }

        @Override
        public IDataSetConverter split() {
            return new TargetTableRows(this.targetTableName, this.rows, this.current);
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
            if (this.targetTableName.equals(this.current[0])) {
                this.rows.add(values[0].toString());
            }
        }
    }

    /**
     * テーブルごとの行を受け取る。xlsのシート出力と同じく分割（split）できない
     */
    private static final class RowsByTable implements IDataSetConverter {

        private final Map<String, List<String>> rows;
        private String current;

        private RowsByTable(final Map<String, List<String>> rows) {
            this.rows = rows;
        }

        @Override
        public boolean isExportEmptyTable() {
            return false;
        }

        @Override
        public boolean isSplittable() {
            return false;
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
            this.startTable(tableMetaData);
        }

        @Override
        public IDataSetConverter split() {
            return this;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.current = metaData.getTableName();
        }

        @Override
        public void endTable() {
            this.current = null;
        }

        @Override
        public void row(final Object[] values) {
            this.rows.computeIfAbsent(this.current, _ -> new ArrayList<>()).add(values[0].toString());
        }
    }
}
