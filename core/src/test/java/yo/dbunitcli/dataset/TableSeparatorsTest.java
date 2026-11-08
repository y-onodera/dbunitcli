package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.TableMetaDataFilter;
import yo.dbunitcli.common.filter.AnyFilter;

import java.util.List;

public class TableSeparatorsTest {

    private static ITableMetaData metaData(final String tableName) {
        return new DefaultTableMetaData(tableName, new Column[]{new Column("key", DataType.UNKNOWN)});
    }

    public static List<String> outputNames(final TableSeparators separators, final String tableName) {
        return separators.getAddSettingTableMetaData(metaData(tableName))
                         .map(AddSettingTableMetaData::getTableName)
                         .toList();
    }

    @Test
    public void onlySettingTargetsはsettingsの対象だけを設定適用後の名前で出力する() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(TableSeparator.builder()
                                         .setTargetFilter(new AnyFilter(List.of("big")))
                                         .setSplitter(TableSplitter.NONE.builder()
                                                                        .setRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder()
                                                                                                   .setNewName("renamed")
                                                                                                   .build())
                                                                        .build())
                                         .build());
        final TableSeparators separators = builder.build().onlySettingTargets();

        Assertions.assertEquals(List.of("renamed"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of(), outputNames(separators, "small"));
    }

    @Test
    public void onlySettingTargetsは変換の無い設定でも対象のテーブルを出力する() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(TableSeparator.builder()
                                         .setTargetFilter(new AnyFilter(List.of("big")))
                                         .build());
        final TableSeparators separators = builder.build().onlySettingTargets();

        Assertions.assertEquals(List.of("big"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of(), outputNames(separators, "small"));
    }

    @Test
    public void onlySettingTargetsはjoinの対象を読み込む() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addJoin(ComparableTableJoinCondition.builder().setLeft("big").setRight("small").build());
        final TableSeparators separators = builder.build().onlySettingTargets();

        Assertions.assertEquals(List.of("big"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of("small"), outputNames(separators, "small"));
        Assertions.assertEquals(List.of(), outputNames(separators, "other"));
    }

    @Test
    public void onlySettingTargetsを指定しなければすべて出力する() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(TableSeparator.builder()
                                         .setTargetFilter(new AnyFilter(List.of("big")))
                                         .build());

        Assertions.assertEquals(List.of("small"), outputNames(builder.build(), "small"));
    }

    @Test
    public void mapで編集しても設定とjoinは重複しない() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(TableSeparator.builder()
                                         .setTargetFilter(new AnyFilter(List.of("big")))
                                         .build());
        builder.addJoin(ComparableTableJoinCondition.builder().setLeft("a").setRight("b").build());

        final TableSeparators mapped = builder.build().map(it -> {
        });

        Assertions.assertEquals(1, mapped.settings().size());
        Assertions.assertEquals(1, mapped.joinConditions().size());
    }

    private static TableSeparator rename(final TableMetaDataFilter target, final TableRenameStrategy rename) {
        return TableSeparator.builder()
                             .setTargetFilter(target)
                             .setSplitter(new TableSplitter.Builder().setRenameFunction(rename).build())
                             .build();
    }

    @Test
    public void 名前を変えても同じ設定に合い続ける場合は1回だけ適用する() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(rename(name -> name.startsWith("T")
                , new TableRenameStrategy.ReplaceFunction.Builder().setPrefix("T_").build()));

        Assertions.assertEquals(List.of("T_T"), outputNames(builder.build(), "T"));
    }

    @Test
    public void 名前を互いに変える設定は1回ずつ適用して止まる() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(rename(new AnyFilter(List.of("A")), new TableRenameStrategy.ReplaceFunction.Builder().setNewName("B").build()));
        builder.addSetting(rename(new AnyFilter(List.of("B")), new TableRenameStrategy.ReplaceFunction.Builder().setNewName("A").build()));

        Assertions.assertEquals(List.of("A"), outputNames(builder.build(), "A"));
    }

    @Test
    public void 共通の名前を変えたあとも共通設定を追加できる() {
        final TableSeparators.Builder builder = TableSeparators.NONE.builder()
                .setCommonRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder().setNewName("TARGET").build());
        builder.addCommon(TableSeparator.builder().setComparisonKeys(List.of("key")).build());

        Assertions.assertEquals(List.of("TARGET"), outputNames(builder.build(), "T"));
    }

    @Test
    public void 同じテーブルに合う設定はsettingsの順に適用する() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        List.of("X1", "X2", "X3", "X4", "X5").forEach(name -> builder.addSetting(rename(new AnyFilter(List.of("T"))
                , new TableRenameStrategy.ReplaceFunction.Builder().setNewName(name).build())));

        Assertions.assertEquals(List.of("X1", "X2", "X3", "X4", "X5"), outputNames(builder.build(), "T"));
    }

    @Test
    public void 空の式だけのfilterは絞り込みの無い設定と同じに扱う() {
        Assertions.assertSame(TableSeparator.NO_FILTER, TableSeparator.RowFilter.of(List.of(" ", "")));
        Assertions.assertFalse(TableSeparator.builder().setFilter(List.of(" ")).build().hasSettings());
    }

    @Test
    public void 大文字小文字だけ違う列があってもincludeColumnsで絞り込める() {
        final TableSeparator separator = TableSeparator.builder().setIncludeColumns(List.of("ID")).build();
        final Column[] columns = {new Column("id", DataType.UNKNOWN), new Column("ID", DataType.UNKNOWN), new Column("V", DataType.UNKNOWN)};
        Assertions.assertEquals(1, separator.filteredColumns(columns, new DefaultTableMetaData("T", columns)).length);
    }
}
