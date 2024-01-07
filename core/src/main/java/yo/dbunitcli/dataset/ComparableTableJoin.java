package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;

import java.util.Arrays;
import java.util.stream.Stream;

public class ComparableTableJoin {
    private final JoinCondition condition;
    private ComparableTable outer;
    private ComparableTable inner;

    public ComparableTableJoin(final JoinCondition condition) {
        this.condition = condition;
    }

    public JoinCondition getCondition() {
        return this.condition;
    }

    public void setOuter(final ComparableTable outer) {
        this.outer = outer;
    }

    public void setInner(final ComparableTable inner) {
        this.inner = inner;
    }

    public boolean hasRelation(final String tableName) {
        return this.getCondition().hasRelation(tableName);
    }

    public Stream<Object[]> execute() {
        return this.outer.stream()
                .flatMap(outerRow -> this.inner.stream()
                        .filter(innerRow -> this.condition.on().apply(outerRow, innerRow))
                        .map(joined -> Stream.concat(outerRow.values().stream(), joined.values().stream()).toArray()));
    }

    public DefaultTableMetaData joinMetaData() {
        return new DefaultTableMetaData(this.outer.getTableMetaData().getTableName() + "_with_" + this.inner.getTableMetaData().getTableName()
                , Stream.concat(this.convertColumnName(this.outer.getTableMetaData())
                        , this.convertColumnName(this.inner.getTableMetaData()))
                .toArray(Column[]::new)
                , this.convertColumnName(this.outer.getTableMetaData(), this.outer.getTableMetaData().getPrimaryKeys())
                .toArray(Column[]::new)
        );
    }

    private Stream<Column> convertColumnName(final AddSettingTableMetaData tableMetaData) {
        return this.convertColumnName(tableMetaData, tableMetaData.getColumns());
    }

    private Stream<Column> convertColumnName(final AddSettingTableMetaData tableMetaData, final Column[] columns) {
        return Arrays.stream(columns).map(it -> new Column(tableMetaData.getTableName() + "_" + it.getColumnName(), it.getDataType()));
    }

}
