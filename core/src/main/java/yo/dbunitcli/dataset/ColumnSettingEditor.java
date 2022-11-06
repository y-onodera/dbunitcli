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
        return this.tableNameMapEdit;
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

    public Consumer<RowFilter.Builder> getFilterEdit() {
        return this.filterEdit;
    }

    public ColumnSettingEditor setTableNameMapEdit(final Function<Function<String, String>, Function<String, String>> function) {
        this.tableNameMapEdit = function;
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

    public ColumnSettingEditor setFilterEdit(final Consumer<RowFilter.Builder> filter) {
        this.filterEdit = filter;
        return this;
    }
}
