package yo.dbunitcli.application.command;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * テスト用のjxlsテンプレート。A1にtableName、2行目からrowsのID・VALを出力する
 */
final class TestJxlsTemplates {

    private TestJxlsTemplates() {
    }

    static File create(final Path dir, final String extension) throws IOException {
        final File result = dir.resolve("template." + extension).toFile();
        try (final Workbook workbook = "xls".equals(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
             final FileOutputStream out = new FileOutputStream(result)) {
            final Sheet sheet = workbook.createSheet("table");
            sheet.createRow(0).createCell(0).setCellValue("${tableName}");
            final Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("${row.ID}");
            row.createCell(1).setCellValue("${row.VAL}");
            TestJxlsTemplates.addComment(sheet, 0, "jx:area(lastCell=\"B2\")");
            TestJxlsTemplates.addComment(sheet, 1, "jx:each(items=\"rows\" var=\"row\" lastCell=\"B2\")");
            workbook.write(out);
        }
        return result;
    }

    private static void addComment(final Sheet sheet, final int rowIndex, final String text) {
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        final ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(0);
        anchor.setCol2(2);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 2);
        final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        sheet.getRow(rowIndex).getCell(0).setCellComment(comment);
    }
}
