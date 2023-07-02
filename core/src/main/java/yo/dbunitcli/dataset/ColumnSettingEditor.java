package yo.dbunitcli.dataset;

import java.util.function.Consumer;

public class ColumnSettingEditor {
    private Consumer<AddSettingColumns.Builder> keyEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> includeEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> excludeEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> orderEdit = (it) -> {
    };

    private Consumer<AddSettingColumns.Builder> expressionEdit = (it) -> {
    };

    private Consumer<TableSeparators.Builder> separatorEdit = (it) -> {
    };

    public Consumer<AddSettingColumns.Builder> getKeyEdit() {
        return this.keyEdit;
    }

    public Consumer<AddSettingColumns.Builder> getIncludeEdit() {
        return this.includeEdit;
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

    public Consumer<TableSeparators.Builder> getSeparatorEdit() {
        return this.separatorEdit;
    }


    public ColumnSettingEditor setKeyEdit(final Consumer<AddSettingColumns.Builder> key) {
        this.keyEdit = key;
        return this;
    }


    public ColumnSettingEditor setIncludeEdit(final Consumer<AddSettingColumns.Builder> include) {
        this.includeEdit = include;
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

    public ColumnSettingEditor setSeparatorEdit(final Consumer<TableSeparators.Builder> filter) {
        this.separatorEdit = filter;
        return this;
    }

}
