package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class ComparableTableJoin {
    private final JoinCondition condition;
    private ComparableTable left;
    private ComparableTable right;

    public ComparableTableJoin(final JoinCondition condition) {
        this.condition = condition;
    }

    public JoinCondition getCondition() {
        return this.condition;
    }

    public void setLeft(final ComparableTable left) {
        this.left = left;
    }

    public void setRight(final ComparableTable right) {
        this.right = right;
    }

    public boolean hasRelation(final String tableName) {
        return this.getCondition().hasRelation(tableName);
    }

    public Stream<Object[]> execute() {
        return this.condition.strategy().execute(this.left, this.right);
    }

    public DefaultTableMetaData joinMetaData() {
        return new DefaultTableMetaData(this.left.getTableMetaData().getTableName() + "_with_" + this.right.getTableMetaData().getTableName()
                , Stream.concat(this.convertColumnName(this.left.getTableMetaData())
                        , this.convertColumnName(this.right.getTableMetaData()))
                .toArray(Column[]::new)
                , this.convertColumnName(this.left.getTableMetaData(), this.left.getTableMetaData().getPrimaryKeys())
                .toArray(Column[]::new)
        );
    }

    private Stream<Column> convertColumnName(final AddSettingTableMetaData tableMetaData) {
        return this.convertColumnName(tableMetaData, tableMetaData.getColumns());
    }

    private Stream<Column> convertColumnName(final AddSettingTableMetaData tableMetaData, final Column[] columns) {
        return Arrays.stream(columns).map(it -> new Column(tableMetaData.getTableName() + "_" + it.getColumnName(), it.getDataType()));
    }

    public interface Strategy {
        Strategy NOT_JOIN = (left, right) -> Stream.empty();

        static Strategy outerJoin(final BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on) {
            return new OuterJoin(on);
        }

        static Strategy innerJoin(final BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on) {
            return new InnerJoin(on);
        }

        Stream<Object[]> execute(ComparableTable left, ComparableTable right);
    }

    private record InnerJoin(BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on) implements Strategy {
        @Override
        public Stream<Object[]> execute(final ComparableTable left, final ComparableTable right) {
            return left.stream()
                    .flatMap(outerRow -> right.stream()
                            .filter(innerRow -> this.on().apply(outerRow, innerRow))
                            .map(joined -> Stream.concat(outerRow.values().stream(), joined.values().stream()).toArray()));
        }
    }

    private record OuterJoin(BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on) implements Strategy {
        @Override
        public Stream<Object[]> execute(final ComparableTable left, final ComparableTable right) {
            return left.stream()
                    .flatMap(outerRow -> {
                        final Object[] firstReturn = new Object[1];
                        final Object[] notJoined = Stream.concat(outerRow.values().stream()
                                , Arrays.stream(new Object[right.getColumnNumbers()])).toArray();
                        return Stream.concat(
                                right.stream()
                                        .filter(innerRow -> this.on().apply(outerRow, innerRow))
                                        .map(innerRow -> {
                                            firstReturn[0] = "";
                                            return Stream.concat(outerRow.values().stream(), innerRow.values().stream()).toArray();
                                        })
                                , Optional.of(notJoined).stream()
                        ).filter(it -> !(firstReturn[0] != null && it == notJoined));
                    });
        }
    }
}
