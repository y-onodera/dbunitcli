package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ComparableTableJoin {
    private final JoinCondition condition;
    private ComparableTable left;
    private ComparableTable right;

    public static Strategy outerJoin(final ConditionBuilder on) {
        return new OuterJoin(on);
    }

    public static Strategy innerJoin(final ConditionBuilder on) {
        return new InnerJoin(on);
    }

    public static Strategy fullJoin(final ConditionBuilder on) {
        return new FullJoin(on);
    }

    public static ConditionBuilder equals(final Set<String> columns) {
        return new Equals(columns);
    }

    public static ConditionBuilder eval(final String expression) {
        return new Eval(expression);
    }

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

    public interface ConditionBuilder {
        BiFunction<Map<String, Object>, Map<String, Object>, Boolean> build(ComparableTable left, ComparableTable right);

    }

    private record Equals(Set<String> columns) implements ConditionBuilder {
        @Override
        public BiFunction<Map<String, Object>, Map<String, Object>, Boolean> build(final ComparableTable left, final ComparableTable right) {
            return (leftRow, rightRow) -> this.columns.stream().allMatch(it -> leftRow.get(it).equals(rightRow.get(it)));
        }
    }

    private record Eval(String expression) implements ConditionBuilder {
        @Override
        public BiFunction<Map<String, Object>, Map<String, Object>, Boolean> build(final ComparableTable left, final ComparableTable right) {
            final JexlExpression jexl = new JexlBuilder().create().createExpression(this.expression());
            return (leftRow, rightRow) -> {
                final Map<String, Object> param = new HashMap<>();
                leftRow.forEach((key, value) -> param.put(left.getTableName() + "_" + key, value));
                rightRow.forEach((key, value) -> param.put(right.getTableName() + "_" + key, value));
                return Boolean.parseBoolean(jexl.evaluate(new MapContext(param)).toString());
            };
        }
    }

    public interface Strategy {
        Strategy NOT_JOIN = (left, right) -> Stream.empty();

        Stream<Object[]> execute(ComparableTable left, ComparableTable right);
    }

    private record InnerJoin(ConditionBuilder condition) implements Strategy {
        @Override
        public Stream<Object[]> execute(final ComparableTable left, final ComparableTable right) {
            final var on = this.condition().build(left, right);
            return left.stream()
                    .flatMap(outerRow -> right.stream()
                            .filter(innerRow -> on.apply(outerRow, innerRow))
                            .map(joined -> Stream.concat(outerRow.values().stream(), joined.values().stream()).toArray()));
        }
    }

    private record OuterJoin(ConditionBuilder condition) implements Strategy {
        @Override
        public Stream<Object[]> execute(final ComparableTable left, final ComparableTable right) {
            final var on = this.condition().build(left, right);
            return left.stream()
                    .flatMap(outerRow -> {
                        final Object[] firstReturn = new Object[1];
                        final Object[] notJoined = Stream.concat(outerRow.values().stream()
                                , Arrays.stream(new Object[right.getColumnNumbers()])).toArray();
                        return Stream.concat(
                                right.stream()
                                        .filter(innerRow -> on.apply(outerRow, innerRow))
                                        .map(innerRow -> {
                                            firstReturn[0] = "";
                                            return Stream.concat(outerRow.values().stream(), innerRow.values().stream()).toArray();
                                        })
                                , Optional.of(notJoined).stream()
                        ).filter(it -> !(firstReturn[0] != null && it == notJoined));
                    });
        }
    }

    private record FullJoin(ConditionBuilder condition) implements Strategy {
        @Override
        public Stream<Object[]> execute(final ComparableTable left, final ComparableTable right) {
            final Set<Integer> joinRows = new HashSet<>();
            final var on = this.condition().build(left, right);
            return Stream.concat(left.stream()
                            .flatMap(outerRow -> {
                                final Object[] firstReturn = new Object[1];
                                final Object[] notJoined = Stream.concat(outerRow.values().stream()
                                        , Arrays.stream(new Object[right.getColumnNumbers()])).toArray();
                                return Stream.concat(
                                        IntStream.range(0, right.getRowCount())
                                                .filter(rowNum -> on.apply(outerRow, right.getRowToMap(rowNum)))
                                                .mapToObj(rowNum -> {
                                                    firstReturn[0] = "";
                                                    joinRows.add(rowNum);
                                                    return Stream.concat(outerRow.values().stream()
                                                            , right.getRowToMap(rowNum).values().stream()).toArray();
                                                })
                                        , Optional.of(notJoined).stream()
                                ).filter(it -> !(firstReturn[0] != null && it == notJoined));
                            })
                    , IntStream.range(0, right.getRowCount())
                            .filter(rowNum -> !joinRows.contains(rowNum))
                            .mapToObj(rest -> Stream.concat(Arrays.stream(new Object[left.getColumnNumbers()])
                                    , right.getRowToMap(rest).values().stream()).toArray())
            );
        }
    }
}
