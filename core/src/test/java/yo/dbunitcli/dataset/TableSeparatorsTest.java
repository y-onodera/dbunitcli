package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.filter.AnyFilter;

import java.util.List;

public class TableSeparatorsTest {

    private static ITableMetaData metaData(final String tableName) {
        return new DefaultTableMetaData(tableName, new Column[]{new Column("key", DataType.UNKNOWN)});
    }

    private static List<String> outputNames(final TableSeparators separators, final String tableName) {
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
}
