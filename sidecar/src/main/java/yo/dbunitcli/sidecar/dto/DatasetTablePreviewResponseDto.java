package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import org.dbunit.dataset.Column;
import yo.dbunitcli.dataset.ComparableTable;

import java.util.Arrays;
import java.util.List;

@Serdeable
public class DatasetTablePreviewResponseDto {

    private String[] headers;
    private List<Object[]> rows;
    /**
     * 対象テーブルの全行数。rowsは先頭の一部だけのため、rows.size()より多いことがある
     */
    private int totalRows;

    public DatasetTablePreviewResponseDto(final String[] headers, final List<Object[]> rows, final int totalRows) {
        this.headers = headers;
        this.rows = rows;
        this.totalRows = totalRows;
    }

    public static DatasetTablePreviewResponseDto empty() {
        return new DatasetTablePreviewResponseDto(new String[0], List.of(), 0);
    }

    /**
     * tableのカラム名をヘッダーにしたプレビュー。rowsは呼び出し側で件数を制限した先頭の一部
     */
    public static DatasetTablePreviewResponseDto of(final ComparableTable table, final List<Object[]> rows, final int totalRows) {
        return new DatasetTablePreviewResponseDto(DatasetTablePreviewResponseDto.headersOf(table), rows, totalRows);
    }

    protected static String[] headersOf(final ComparableTable table) {
        return Arrays.stream(table.getTableMetaData().getColumns())
                     .map(Column::getColumnName)
                     .toArray(String[]::new);
    }

    public String[] getHeaders() {
        return this.headers;
    }

    public void setHeaders(final String[] headers) {
        this.headers = headers;
    }

    public List<Object[]> getRows() {
        return this.rows;
    }

    public void setRows(final List<Object[]> rows) {
        this.rows = rows;
    }

    public int getTotalRows() {
        return this.totalRows;
    }

    public void setTotalRows(final int totalRows) {
        this.totalRows = totalRows;
    }
}
