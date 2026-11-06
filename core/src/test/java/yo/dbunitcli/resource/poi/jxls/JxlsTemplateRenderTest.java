package yo.dbunitcli.resource.poi.jxls;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.common.Parameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

class JxlsTemplateRenderTest {

    private static byte[] template(final String... cells) throws IOException {
        try (final Workbook workbook = new XSSFWorkbook(); final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final Sheet sheet = workbook.createSheet("Sheet1");
            final Row row = sheet.createRow(0);
            for (int i = 0; i < cells.length; i++) {
                row.createCell(i).setCellValue(cells[i]);
            }
            final ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
            comment.setString(workbook.getCreationHelper().createRichTextString(
                    "jx:area(lastCell=\"" + (char) ('A' + cells.length - 1) + "1\")"));
            row.getCell(0).setCellComment(comment);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void templateExpressionCanUseTryToNum(@TempDir final File dir) throws IOException {
        final File result = new File(dir, "result.xlsx");
        JxlsTemplateRender.builder()
                .setTemplateParameterAttribute("")
                .build()
                .render(template("${tryToNum(a, 0) + 1}", "${tryToNum(b, 'N/A')}", "${a}")
                        , result, new Parameter(Map.of("a", "1,234", "b", "abc")), false);
        try (final Workbook workbook = WorkbookFactory.create(result)) {
            final Row row = workbook.getSheetAt(0).getRow(0);
            Assertions.assertEquals(1235d, row.getCell(0).getNumericCellValue());
            Assertions.assertEquals("N/A", row.getCell(1).getStringCellValue());
            Assertions.assertEquals("1,234", row.getCell(2).getStringCellValue());
        }
    }
}
