package yo.dbunitcli.dataset;

import java.util.function.Consumer;
import java.util.function.Function;

public class ColumnSettingEditor {
    private Function<Function<String, String>, Function<String, String>> tableNameMapEdit = it -> it;

    private Consumer<AddSettingColumns.Builder> keyEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> excludeEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> orderEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> expressionEdit = (it) -> {
    };

    private Consumer<RowFilter.Builder> filterEdit = (it) -> {
    };

    public Function<Function<String, String>, Function<String, String>> getTableNameMapEdit() {
        return tableNameMapEdit;
    }

    public Consumer<AddSettingColumns.Builder> getKeyEdit() {
        return keyEdit;
    }

    public Consumer<AddSettingColumns.Builder> getExcludeEdit() {
        return excludeEdit;
    }

    public Consumer<AddSettingColumns.Builder> getOrderEdit() {
        return orderEdit;
    }

    public Consumer<AddSettingColumns.Builder> getExpressionEdit() {
        return expressionEdit;
    }

    public Consumer<RowFilter.Builder> getFilterEdit() {
        return filterEdit;
    }

    public ColumnSettingEditor setTableNameMapEdit(Function<Function<String, String>, Function<String, String>> function) {
        this.tableNameMapEdit = function;
        return this;
    }

    public ColumnSettingEditor setKeyEdit(Consumer<AddSettingColumns.Builder> key) {
        this.keyEdit = key;
        return this;
    }

    public ColumnSettingEditor setExcludeEdit(Consumer<AddSettingColumns.Builder> exclude) {
        this.excludeEdit = exclude;
        return this;
    }

    public ColumnSettingEditor setOrderEdit(Consumer<AddSettingColumns.Builder> order) {
        this.orderEdit = order;
        return this;
    }

    public ColumnSettingEditor setExpressionEdit(Consumer<AddSettingColumns.Builder> expression) {
        this.expressionEdit = expression;
        return this;
    }

    public ColumnSettingEditor setFilterEdit(Consumer<RowFilter.Builder> filter) {
        this.filterEdit = filter;
        return this;
    }
}
