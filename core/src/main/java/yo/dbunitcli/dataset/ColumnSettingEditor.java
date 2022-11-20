package yo.dbunitcli.dataset;

import java.util.function.Consumer;
import java.util.function.Function;

public class ColumnSettingEditor {
    private Function<Function<String, String>, Function<String, String>> tableRenameFunctionEdit = Function.identity();

    private Consumer<AddSettingColumns.Builder> keyEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> excludeEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> orderEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> expressionEdit = (it) -> {
    };

    private Consumer<RowFilters.Builder> filterEdit = (it) -> {
    };

    private Consumer<TableSplitter.Builder> getTableSplitterEdit = (it) -> {
    };

    public Function<Function<String, String>, Function<String, String>> getTableRenameFunctionEdit() {
        return this.tableRenameFunctionEdit;
    }

    public Consumer<AddSettingColumns.Builder> getKeyEdit() {
        return this.keyEdit;
    }

    public Consumer<AddSettingColumns.Builder> getExcludeEdit() {
        return this.excludeEdit;
    }

    public Consumer<AddSettingColumns.Builder> getOrderEdit() {
        return this.orderEdit;
    }

    public Consumer<AddSettingColumns.Builder> getExpressionEdit() {
        return this.expressionEdit;
    }

    public Consumer<RowFilters.Builder> getFilterEdit() {
        return this.filterEdit;
    }

    public Consumer<TableSplitter.Builder> getTableSplitterEdit() {
        return this.getTableSplitterEdit;
    }

    public ColumnSettingEditor setTableRenameFunctionEdit(final Function<Function<String, String>, Function<String, String>> function) {
        this.tableRenameFunctionEdit = function;
        return this;
    }

    public ColumnSettingEditor setKeyEdit(final Consumer<AddSettingColumns.Builder> key) {
        this.keyEdit = key;
        return this;
    }

    public ColumnSettingEditor setExcludeEdit(final Consumer<AddSettingColumns.Builder> exclude) {
        this.excludeEdit = exclude;
        return this;
    }

    public ColumnSettingEditor setOrderEdit(final Consumer<AddSettingColumns.Builder> order) {
        this.orderEdit = order;
        return this;
    }

    public ColumnSettingEditor setExpressionEdit(final Consumer<AddSettingColumns.Builder> expression) {
        this.expressionEdit = expression;
        return this;
    }

    public ColumnSettingEditor setFilterEdit(final Consumer<RowFilters.Builder> filter) {
        this.filterEdit = filter;
        return this;
    }

    public ColumnSettingEditor setGetTableSplitterEdit(final Consumer<TableSplitter.Builder> getTableSplitterEdit) {
        this.getTableSplitterEdit = getTableSplitterEdit;
        return this;
    }
}
