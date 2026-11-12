package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.Column;
import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JxlsTemplateGenerator {

    @SuppressWarnings("unchecked")
    public static void createTemplate(final File resultFile, final Parameter param) throws IOException {
        final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
        try (final Workbook workbook = new XSSFWorkbook()) {
            for (final Map.Entry<String, Map<String, Object>> tableEntry : dataSet.entrySet()) {
                final String tableName = tableEntry.getKey();
                final Map<String, Object> tableData = tableEntry.getValue();
                final List<String> columns = Arrays.stream((Column[]) tableData.get("columns"))
                        .map(Column::getColumnName)
                        .toList();
                final Sheet sheet = workbook.createSheet(tableName);
                createRow(sheet, 0, columns);
                createRow(sheet, 1, columns.stream().map(JxlsTemplateGenerator::rowValue).toList());
                addAreaComments(sheet, 0, 1, columns.size() - 1);
            }
            write(workbook, resultFile);
        }
    }

    static void write(final Workbook workbook, final File resultFile) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(resultFile)) {
            workbook.write(fos);
        }
    }

    /** jx:eachの1行分で列の値を出す式 */
    static String rowValue(final String columnName) {
        return "${row.get('" + columnName + "')}";
    }

    static Row createRow(final Sheet sheet, final int rowIndex, final List<?> values) {
        final Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++) {
            row.createCell(i).setCellValue(String.valueOf(values.get(i)));
        }
        return row;
    }

    /**
     * jxlsのパラメータ化数式（$[…]）として書く数式（先頭の"="なし）。
     * 通常の数式セルはjxlsがセル参照を含まないものを0に置き換え、formulaProcess=falseでは参照をずらさないため使わない
     */
    record Formula(String text) {
        @Override
        public String toString() {
            return "$[" + this.text + "]";
        }
    }

    /**
     * A{areaRow+1}からeach行の最終列までをjx:area、each行をシート名のテーブルの全行を回すjx:eachにする
     */
    static void addAreaComments(final Sheet sheet, final int areaRow, final int eachRow, final int lastColumn) {
        addAreaComments(sheet, areaRow, eachRow, lastColumn, sheet.getSheetName());
    }

    static void addAreaComments(final Sheet sheet, final int areaRow, final int eachRow, final int lastColumn, final String tableName) {
        final String lastCell = new CellReference(eachRow, lastColumn).formatAsString();
        addComment(sheet, areaRow, "jx:area(lastCell=\"" + lastCell + "\")");
        addComment(sheet, eachRow, "jx:each(items=\"param.dataSet.get('" + tableName + "').rows\" var=\"row\" varIndex=\"index\" lastCell=\"" + lastCell + "\")");
    }

    private static void addComment(final Sheet sheet, final int rowIndex, final String commentText) {
        final Drawing<?> drawing = sheet.createDrawingPatriarch();
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();

        final ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(0);
        anchor.setRow1(rowIndex);
        anchor.setCol2(0);
        anchor.setRow2(rowIndex);

        final Comment comment = drawing.createCellComment(anchor);
        comment.setString(factory.createRichTextString(commentText));
        final Cell cell = sheet.getRow(rowIndex).getCell(0);
        cell.setCellComment(comment);
    }

}
