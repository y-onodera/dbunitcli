package yo.dbunitcli.dataset.compare;

import yo.dbunitcli.dataset.CompareKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface RowCompareResultHandler {

    void handleModify(Object[] oldRow, Object[] newRow, CompareKeys key);

    void handleDelete(int rowNum, Object[] row);

    void handleAdd(int rowNum, Object[] row);

    List<CompareDiff> result();

    default RowCompareResultHandler compose(RowCompareResultHandler handlers) {
        RowCompareResultHandler origin = this;
        return new RowCompareResultHandler() {

            @Override
            public void handleModify(Object[] oldRow, Object[] newRow, CompareKeys key) {
                Stream.of(origin, handlers).forEach(it -> it.handleModify(oldRow, newRow, key));
            }

            @Override
            public void handleDelete(int rowNum, Object[] row) {
                Stream.of(origin, handlers).forEach(it -> it.handleDelete(rowNum, row));
            }

            @Override
            public void handleAdd(int rowNum, Object[] row) {
                Stream.of(origin, handlers).forEach(it -> it.handleAdd(rowNum, row));
            }

            @Override
            public List<CompareDiff> result() {
                List<CompareDiff> results = new ArrayList<>();
                Stream.of(origin, handlers).forEach(it -> results.addAll(it.result()));
                return results;
            }
        };
    }
}
