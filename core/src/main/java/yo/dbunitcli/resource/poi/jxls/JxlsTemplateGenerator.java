package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dbunit.dataset.Column;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;

public class JxlsTemplateGenerator {

    @SuppressWarnings("unchecked")
    public static void createTemplate(final File resultFile, final Map<String, Object> param) throws IOException {
        final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
        try (final Workbook workbook = new XSSFWorkbook()) {
            for (final Map.Entry<String, Map<String, Object>> tableEntry : dataSet.entrySet()) {
                final String tableName = tableEntry.getKey();
                final Map<String, Object> tableData = tableEntry.getValue();
                final Column[] columns = (Column[]) tableData.get("columns");
                final Sheet sheet = workbook.createSheet(tableName);
                createHeaderRow(sheet, columns);
                createJxlsRow(sheet, columns);
                addJxlsComments(sheet, new CellReference(1, columns.length - 1).formatAsString());
            }
            try (final FileOutputStream fos = new FileOutputStream(resultFile)) {
                workbook.write(fos);
            }
        }
    }

    private static void createHeaderRow(final Sheet sheet, final Column[] columns) {
        final Row headerRow = sheet.createRow(0);
        IntStream.range(0, columns.length)
                .forEach(i -> {
                    final Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i].getColumnName());
                });
    }

    private static void createJxlsRow(final Sheet sheet, final Column[] columns) {
        final Row dataRow = sheet.createRow(1);
        IntStream.range(0, columns.length)
                .forEach(i -> {
                    final Cell cell = dataRow.createCell(i);
                    cell.setCellValue("${row." + columns[i].getColumnName() + "}");
                });
    }

    private static void addJxlsComments(final Sheet sheet, final String lastCell) {
        addComment(sheet, 0, "jx:area(lastCell=\"" + lastCell + "\")");
        addComment(sheet, 1, "jx:each(items=\"param.dataSet.get('" + sheet.getSheetName() + "').rows\" var=\"row\" varIndex=\"index\" lastCell=\"" + lastCell + "\")");
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