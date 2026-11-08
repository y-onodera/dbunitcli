package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableSplitterTest {

    private static TableSplitter splitByBreakKey() {
        return new TableSplitter.Builder()
                .setRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder()
                        .setSuffix("_%d")
                        .setSplit(true)
                        .build())
                .setLimit(1)
                .setBreakKeys(List.of("DEPT"))
                .build();
    }

    @Test
    public void builderはbreakKeyを引き継ぐ() {
        final TableSplitter splitter = splitByBreakKey();
        assertEquals(List.of("DEPT"), splitter.builder().build().breakKeys());
    }

    @Test
    public void 名前の設定と合わせてもbreakKeyを引き継ぐ() {
        final TableSeparator rename = TableSeparator.builder()
                .setSplitter(new TableSplitter.Builder()
                        .setRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder().setPrefix("P_").build())
                        .build())
                .build();
        final TableSeparator split = TableSeparator.builder().setSplitter(splitByBreakKey()).build();
        assertEquals(List.of("DEPT"), rename.add(split).splitter().breakKeys());
        assertEquals(List.of("DEPT"), split.add(rename).splitter().breakKeys());
    }

    @Test
    public void breakKeyの値がnullでも区切りを判定できる() {
        final TableSplitter splitter = splitByBreakKey();
        final AddSettingTableMetaData metaData = TableSeparator.NONE.addSetting(new DefaultTableMetaData("T", new Column[]{
                new Column("DEPT", DataType.UNKNOWN), new Column("NAME", DataType.UNKNOWN)}));
        final var reader = splitter.breakKeyReader(metaData);
        final TableSplitter.Counter counter = splitter.counter();
        assertEquals(Arrays.asList((String) null), reader.apply(new Object[]{null, "a"}));
        assertEquals(false, counter.isBreak(reader.apply(new Object[]{null, "a"}), 0));
        assertEquals(false, counter.isBreak(reader.apply(new Object[]{null, "b"}), 1));
        assertEquals(true, counter.isBreak(reader.apply(new Object[]{"1", "c"}), 2));
    }
}
