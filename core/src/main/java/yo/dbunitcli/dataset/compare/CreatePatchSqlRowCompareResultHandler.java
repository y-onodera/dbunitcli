package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.Column;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.application.command.GenerateType;
import yo.dbunitcli.dataset.CompareKeys;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CreatePatchSqlRowCompareResultHandler implements RowCompareResultHandler {

    private static final String FILE_SUFFIX = "$PATCH.sql";

    private final TableCompare tableCompare;
    private final Column[] columns;
    private final Column[] primaryKeys;
    private final Column[] columnsExcludeKey;
    private final List<Map<String, Object>> updateRows = new ArrayList<>();
    private final List<Map<String, Object>> deleteRows = new ArrayList<>();
    private final List<Map<String, Object>> insertRows = new ArrayList<>();

    protected CreatePatchSqlRowCompareResultHandler(final TableCompare tableCompare) {
        this.tableCompare = tableCompare;
        this.columns = IntStream.range(0, tableCompare.getColumnLength())
                .mapToObj(tableCompare::getOldColumn)
                .toArray(Column[]::new);
        final List<String> keyColumns = tableCompare.getKeyColumns();
        this.primaryKeys = Arrays.stream(this.columns)
                .filter(it -> keyColumns.contains(it.getColumnName()))
                .toArray(Column[]::new);
        this.columnsExcludeKey = Arrays.stream(this.columns)
                .filter(it -> !keyColumns.contains(it.getColumnName()))
                .toArray(Column[]::new);
    }

    @Override
    public void handleModify(final Object[] oldRow, final Object[] newRow, final CompareKeys key) {
        if (this.primaryKeys.length == 0) {
            return;
        }
        final boolean changed = IntStream.range(0, this.columns.length)
                .anyMatch(i -> !Objects.equals(this.toString(oldRow[i]), this.toString(newRow[i])));
        if (changed) {
            this.updateRows.add(this.toRowMap(newRow));
        }
    }

    @Override
    public void handleDelete(final int rowNum, final Object[] row) {
        if (this.primaryKeys.length == 0) {
            return;
        }
        this.deleteRows.add(this.toRowMap(row));
    }

    @Override
    public void handleAdd(final int rowNum, final Object[] row) {
        if (this.primaryKeys.length == 0) {
            return;
        }
        this.insertRows.add(this.toRowMap(row));
    }

    @Override
    public List<CompareDiff> result() {
        if (!this.updateRows.isEmpty() || !this.deleteRows.isEmpty() || !this.insertRows.isEmpty()) {
            this.write();
        }
        return new ArrayList<>();
    }

    protected Map<String, Object> toRowMap(final Object[] row) {
        final Map<String, Object> result = new LinkedHashMap<>();
        IntStream.range(0, this.columns.length)
                .forEach(i -> result.put(this.columns[i].getColumnName(), row[i]));
        return result;
    }

    protected String toString(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof final BigDecimal num) {
            return num.toPlainString();
        }
        return value.toString();
    }

    protected void write() {
        final File dir = this.tableCompare.getConverter().getDir();
        if (dir == null) {
            return;
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new AssertionError("fail to create directory:" + dir);
        }
        final STGroup stGroup = GenerateType.sql.getStGroup();
        final StringBuilder sql = new StringBuilder();
        sql.append(this.render(stGroup, "update", this.updateRows));
        sql.append(this.render(stGroup, "insert", this.insertRows));
        sql.append(this.render(stGroup, "delete", this.deleteRows));
        final File sqlFile = new File(dir, this.tableCompare.getTableName() + CreatePatchSqlRowCompareResultHandler.FILE_SUFFIX);
        try {
            Files.writeString(sqlFile.toPath(), sql.toString(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String render(final STGroup stGroup, final String templateName, final List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "";
        }
        final ST st = new ST(stGroup, "$" + templateName + "()$");
        st.add("tableName", this.tableCompare.getTableName());
        st.add("columns", this.columns);
        st.add("primaryKeys", this.primaryKeys);
        st.add("columnsExcludeKey", this.columnsExcludeKey);
        st.add("rows", rows.stream().collect(Collectors.toList()));
        return st.render();
    }
}
