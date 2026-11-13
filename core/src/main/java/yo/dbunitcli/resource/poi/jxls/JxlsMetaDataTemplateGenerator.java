package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * テーブル定義書のjxlsテンプレートを作る。レイアウトはsrcに依らず固定で、unit=tableで
 * ComparableDdlMetaDataProducerの記述子行（列ごとに1行）を流し込む前提（1テーブルが1ファイル）
 */
public final class JxlsMetaDataTemplateGenerator {

    private static final String SHEET_NAME = "テーブル定義";
    private static final int TABLE_NAME_ROW = 0;
    private static final int TABLE_REMARKS_ROW = 1;
    private static final int HEADER_ROW = 3;
    private static final int EACH_ROW = 4;
    private static final int LABEL_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    private static final List<ColumnDef> COLUMNS = List.of(
            new ColumnDef("No", "${index + 1}", 5)
            , new ColumnDef("カラム名", JxlsTemplateGenerator.rowValue("COLUMN_NAME"), 30)
            , new ColumnDef("型", JxlsTemplateGenerator.rowValue("TYPE_NAME"), 16)
            , new ColumnDef("桁", JxlsTemplateGenerator.rowValue("COLUMN_SIZE"), 8)
            , new ColumnDef("小数", JxlsTemplateGenerator.rowValue("DECIMAL_DIGITS"), 8)
            , new ColumnDef("NOT NULL", markWhen("NULLABLE", false), 10)
            , new ColumnDef("PK", markWhen("IS_PK", true), 6)
            , new ColumnDef("コメント", JxlsTemplateGenerator.rowValue("REMARKS"), 40)
    );

    private JxlsMetaDataTemplateGenerator() {
    }

    public static void write(final File resultFile) throws IOException {
        try (final Workbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet(SHEET_NAME);
            final CellStyle headerStyle = headerStyle(workbook);
            final CellStyle valueStyle = borderStyle(workbook);
            labeledRow(sheet, TABLE_NAME_ROW, "テーブル名", "${param.tableName}", headerStyle, valueStyle);
            labeledRow(sheet, TABLE_REMARKS_ROW, "コメント", JxlsTemplateGenerator.rowValue("TABLE_REMARKS"), headerStyle, valueStyle);
            final Row header = sheet.createRow(HEADER_ROW);
            final Row each = sheet.createRow(EACH_ROW);
            for (int i = 0; i < COLUMNS.size(); i++) {
                final ColumnDef column = COLUMNS.get(i);
                createCell(header, i, column.header(), headerStyle);
                createCell(each, i, column.expression(), valueStyle);
                sheet.setColumnWidth(i, column.width() * 256);
            }
            final String lastCell = new CellReference(EACH_ROW, COLUMNS.size() - 1).formatAsString();
            JxlsTemplateGenerator.addComment(sheet, TABLE_NAME_ROW, LABEL_COLUMN, "jx:area(lastCell=\"" + lastCell + "\")");
            // TABLE_REMARKSは全行同じ値を持つため、最初の1行だけから取る（rowsは出力中に読むため添字で取れない）
            JxlsTemplateGenerator.addComment(sheet, TABLE_REMARKS_ROW, VALUE_COLUMN
                    , eachComment("select=\"index == 0\" ", new CellReference(TABLE_REMARKS_ROW, VALUE_COLUMN).formatAsString()));
            JxlsTemplateGenerator.addComment(sheet, EACH_ROW, LABEL_COLUMN, eachComment("", lastCell));
            JxlsTemplateGenerator.write(workbook, resultFile);
        }
    }

    private static String eachComment(final String option, final String lastCell) {
        return "jx:each(items=\"param.rows\" var=\"row\" varIndex=\"index\" " + option + "lastCell=\"" + lastCell + "\")";
    }

    // 記述子行の真偽値はソースによってBoolean・文字列・空文字のいずれかになるため、文字列にして比べる
    private static String markWhen(final String columnName, final boolean value) {
        return "${row.get('" + columnName + "').toString() == '" + value + "' ? '○' : ''}";
    }

    private static void labeledRow(final Sheet sheet, final int rowIndex, final String label, final String value
            , final CellStyle labelStyle, final CellStyle valueStyle) {
        final Row row = sheet.createRow(rowIndex);
        createCell(row, LABEL_COLUMN, label, labelStyle);
        createCell(row, VALUE_COLUMN, value, valueStyle);
    }

    private static void createCell(final Row row, final int column, final String value, final CellStyle style) {
        final Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static CellStyle headerStyle(final Workbook workbook) {
        final CellStyle style = borderStyle(workbook);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle borderStyle(final Workbook workbook) {
        final CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * @param width 列幅（文字数）
     */
    private record ColumnDef(String header, String expression, int width) {
    }
}
