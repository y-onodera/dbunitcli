package yo.dbunitcli.dataset.converter;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AppendConverterTest {

    private static final DefaultTableMetaData TABLE = new DefaultTableMetaData("t1", new Column[]{
            new Column("key", DataType.UNKNOWN), new Column("col1", DataType.UNKNOWN)});

    private static void write(final IDataSetConverter converter, final String... keys) throws DataSetException {
        AppendConverterTest.write(converter, AppendConverterTest.TABLE, keys);
    }

    private static void write(final IDataSetConverter converter, final DefaultTableMetaData table, final String... keys)
            throws DataSetException {
        converter.startDataSet();
        converter.startTable(table);
        for (final String key : keys) {
            converter.row(new Object[]{key, "v" + key});
        }
        converter.endTable();
        converter.endDataSet();
    }

    private static List<String> cells(final File file, final String sheetName) throws IOException {
        try (final FileInputStream in = new FileInputStream(file); final Workbook workbook = WorkbookFactory.create(in)) {
            final Sheet sheet = workbook.getSheet(sheetName);
            final DataFormatter formatter = new DataFormatter();
            final List<String> result = new ArrayList<>();
            IntStream.rangeClosed(0, sheet.getLastRowNum()).forEach(i -> {
                final Row row = sheet.getRow(i);
                result.add(row == null ? "" : formatter.formatCellValue(row.getCell(0)) + "," + formatter.formatCellValue(row.getCell(1)));
            });
            return result;
        }
    }

    @Nested
    class Csv {

        @TempDir
        Path dir;

        private CsvConverter converter(final boolean append) {
            return new CsvConverter(this.dir.toString(), this.dir.toFile(), "UTF-8", true, true, append, null);
        }

        @Test
        public void appendRowsWithoutHeaderToExistingFile() throws Exception {
            AppendConverterTest.write(this.converter(false), "1");
            AppendConverterTest.write(this.converter(true), "2", "3");
            Assertions.assertEquals(List.of("\"key\",\"col1\"", "\"1\",\"v1\"", "\"2\",\"v2\"", "\"3\",\"v3\"")
                    , Files.readAllLines(this.dir.resolve("t1.csv"), StandardCharsets.UTF_8));
        }

        @Test
        public void createNewFileWithHeaderWhenNotExists() throws Exception {
            AppendConverterTest.write(this.converter(true), "1");
            Assertions.assertEquals(List.of("\"key\",\"col1\"", "\"1\",\"v1\"")
                    , Files.readAllLines(this.dir.resolve("t1.csv"), StandardCharsets.UTF_8));
        }

        @Test
        public void overwriteWhenAppendIsFalse() throws Exception {
            AppendConverterTest.write(this.converter(false), "1");
            AppendConverterTest.write(this.converter(false), "2");
            Assertions.assertEquals(List.of("\"key\",\"col1\"", "\"2\",\"v2\"")
                    , Files.readAllLines(this.dir.resolve("t1.csv"), StandardCharsets.UTF_8));
        }
    }

    @Nested
    class Excel {

        @TempDir
        Path dir;

        private IDataSetConverter converter(final boolean xlsx, final XlsConverter.TableExportType type, final boolean append) {
            return xlsx ? new XlsxConverter(this.dir.toFile(), "book", type, true, true, append)
                    : new XlsConverter(this.dir.toFile(), "book", type, true, true, append);
        }

        private void assertAppend(final boolean xlsx, final XlsConverter.TableExportType type, final String fileName) throws Exception {
            AppendConverterTest.write(this.converter(xlsx, type, false), "1");
            AppendConverterTest.write(this.converter(xlsx, type, true), "2", "3");
            Assertions.assertEquals(List.of("key,col1", "1,v1", "2,v2", "3,v3")
                    , AppendConverterTest.cells(this.dir.resolve(fileName).toFile(), "t1"));
        }

        @Test
        public void xlsSheet() throws Exception {
            this.assertAppend(false, XlsConverter.TableExportType.SHEET, "book.xls");
        }

        @Test
        public void xlsBook() throws Exception {
            this.assertAppend(false, XlsConverter.TableExportType.BOOK, "t1.xls");
        }

        @Test
        public void xlsxSheet() throws Exception {
            this.assertAppend(true, XlsConverter.TableExportType.SHEET, "book.xlsx");
        }

        @Test
        public void xlsxBook() throws Exception {
            this.assertAppend(true, XlsConverter.TableExportType.BOOK, "t1.xlsx");
        }

        @Test
        public void addSheetWithHeaderWhenSheetNotExists() throws Exception {
            final DefaultTableMetaData other = new DefaultTableMetaData("t2", AppendConverterTest.TABLE.getColumns());
            AppendConverterTest.write(this.converter(true, XlsConverter.TableExportType.SHEET, false), other, "9");
            AppendConverterTest.write(this.converter(true, XlsConverter.TableExportType.SHEET, true), "1");
            final File book = this.dir.resolve("book.xlsx").toFile();
            Assertions.assertEquals(List.of("key,col1", "9,v9"), AppendConverterTest.cells(book, "t2"));
            Assertions.assertEquals(List.of("key,col1", "1,v1"), AppendConverterTest.cells(book, "t1"));
        }
    }
}
