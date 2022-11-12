package yo.dbunitcli.dataset.compare;

import yo.dbunitcli.dataset.CompareKeys;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RowCompareResultHandler {

    void handleModify(Object[] oldRow, Object[] newRow, CompareKeys key);

    void handleDelete(int rowNum, Object[] row);

    void handleAdd(int rowNum, Object[] row);

    List<CompareDiff> result();

    default RowCompareResultHandler compose(final RowCompareResultHandler handlers) {
        final RowCompareResultHandler origin = this;
        return new RowCompareResultHandler() {

            @Override
            public void handleModify(final Object[] oldRow, final Object[] newRow, final CompareKeys key) {
                Stream.of(origin, handlers).forEach(it -> it.handleModify(oldRow, newRow, key));
            }

            @Override
            public void handleDelete(final int rowNum, final Object[] row) {
                Stream.of(origin, handlers).forEach(it -> it.handleDelete(rowNum, row));
            }

            @Override
            public void handleAdd(final int rowNum, final Object[] row) {
                Stream.of(origin, handlers).forEach(it -> it.handleAdd(rowNum, row));
            }

            @Override
            public List<CompareDiff> result() {
                return Stream.of(origin, handlers).map(RowCompareResultHandler::result)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }
        };
    }
}
