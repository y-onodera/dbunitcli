package yo.dbunitcli.application;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.*;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class ConvertTest {

    private static final Project PROJECT = new Project();
    private static final Properties backup = new Properties();
    private static String testResourceDir;
    private static String baseDir;

    private static String[] getColumnNames(final ITable split1) throws DataSetException {
        return Arrays.stream(split1.getTableMetaData().getColumns())
                .map(Column::getColumnName)
                .toArray(String[]::new);
    }

    private static void copy(final String from, final String to) {
        final Copy copy = new Copy();
        copy.setProject(ConvertTest.PROJECT);
        final FileSet src = new FileSet();
        src.setDir(new File(from));
        copy.addFileset(src);
        copy.setTodir(new File(to));
        copy.execute();
    }

    private static void clean(final String target) {
        final Delete delete = new Delete();
        delete.setProject(ConvertTest.PROJECT);
        delete.setDir(new File(target));
        delete.execute();
    }

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        ConvertTest.baseDir = URLDecoder.decode(Objects.requireNonNull(ConvertTest.class.getResource(".")).getPath(), StandardCharsets.UTF_8);
        ConvertTest.testResourceDir = ConvertTest.baseDir.replace("target/test-classes", "src/test/resources");
        ConvertTest.PROJECT.setName("convertTest");
        ConvertTest.PROJECT.setBaseDir(new File("."));
        ConvertTest.PROJECT.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
        ConvertTest.backup.putAll(System.getProperties());
    }

    abstract static class TestCase {
        @AfterAll
        static void restore() {
            System.setProperties(backup);
        }

        @Test
        public void testFromRegexToXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertRegexToXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/regex2xlsx/result/paramConvertRegexToXlsx.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            Assertions.assertEquals("dirty", actual.getTableNames()[0]);
        }

        @Test
        public void testNoSetting() throws Exception {
            Convert.main(this.getArgs("/paramConvertRegexToXlsxNoSetting.txt"));
            final File src = new File(this.getBaseDir() + "/convert/regex2xlsx/result/paramConvertRegexToXlsxNoSetting.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            Assertions.assertEquals("dirty", actual.getTableNames()[0]);
        }

        @Test
        public void testFromCsvToXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvToXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/csv2xlsx/result/paramConvertCsvToXlsx.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(2, actual.getTableNames().length);
            Assertions.assertEquals("multi1", actual.getTableNames()[0]);
            Assertions.assertEquals("multi2", actual.getTableNames()[1]);
        }

        @Test
        public void testFromCsvIgnoreQuoteToXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvIgnoreQuotedToXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/csv2xlsx/quoted/result/paramConvertCsvIgnoreQuotedToXlsx.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable table = actual.getTable("quoted");
            Assertions.assertEquals(3, table.getRowCount());
            Assertions.assertEquals("\"1\"", table.getValue(0, "\"key\""));
            Assertions.assertEquals("\"\"2\"\"", table.getValue(0, "\"column1\""));
            Assertions.assertEquals("\"3\"", table.getValue(0, "\"column2\""));
            Assertions.assertEquals("\"4\"", table.getValue(0, "\"column3\""));
        }

        @Test
        public void testFromCsvToMultiXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvToMultiXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/csv2xlsx/resultmultixlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.xlsx)
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(2, actual.getTableNames().length);
            Assertions.assertEquals("multi1", actual.getTableNames()[0]);
            Assertions.assertEquals("multi2", actual.getTableNames()[1]);
        }

        @Test
        public void testFromCsvToMultiXls() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvToMultiXls.txt"));
            final File src = new File(this.getBaseDir() + "/convert/csv2xls/resultmultixls");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.xls)
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(2, actual.getTableNames().length);
            Assertions.assertEquals("multi1", actual.getTableNames()[0]);
            Assertions.assertEquals("multi2", actual.getTableNames()[1]);
        }

        @Test
        public void testFromFixedFileToCsv() throws Exception {
            Convert.main(this.getArgs("/paramConvertFixedFileToCsv.txt"));
            final File src = new File(this.getBaseDir() + "/convert/fixed2csv/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.csv)
                                    .setSrc(src)
                                    .setEncoding("UTF-8")
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            Assertions.assertEquals("固定長ファイル", actual.getTableNames()[0]);
            final ComparableTable table = actual.getTable("固定長ファイル");
            Assertions.assertEquals(4, table.getRowCount());
            Assertions.assertEquals("a1a", table.getValue(0, "半角"));
            Assertions.assertEquals("a  ", table.getValue(1, "半角"));
            Assertions.assertEquals("   ", table.getValue(2, "半角"));
            Assertions.assertEquals("   ", table.getValue(3, "半角"));
            Assertions.assertEquals("123                                               ", table.getValue(0, "数値"));
            Assertions.assertEquals("                                                  ", table.getValue(1, "数値"));
            Assertions.assertEquals("123                                               ", table.getValue(2, "数値"));
            Assertions.assertEquals("                                                  ", table.getValue(3, "数値"));
            Assertions.assertEquals("あさぼらけ", table.getValue(0, "全角"));
            Assertions.assertEquals("      有明の", table.getValue(1, "全角"));
            Assertions.assertEquals("              1", table.getValue(2, "全角"));
            Assertions.assertEquals("         月と", table.getValue(3, "全角"));
        }

        @Test
        public void testFromCsvqToCsv() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvqToCsv.txt"));
            final File src = new File(this.getBaseDir() + "/convert/csvq2csv/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.csv)
                                    .setSrc(src)
                                    .setEncoding("Shift-Jis")
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            Assertions.assertEquals("joinQuery", actual.getTableNames()[0]);
        }

        @Test
        public void testFromXlsxWithSchemaToCsv() throws Exception {
            Convert.main(this.getArgs("/paramConvertXlsxWithSchemaToCsv.txt"));
            final File src = new File(this.getBaseDir() + "convert/xlsxwithschema2csv/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.csv)
                                    .setSrc(src)
                                    .setEncoding("Shift-Jis")
                                    .build()));
            Assertions.assertEquals(4, actual.getTableNames().length);
            Assertions.assertEquals("テーブル一覧", actual.getTableNames()[0]);
            Assertions.assertEquals("ユーザマスタ", actual.getTableNames()[1]);
            Assertions.assertEquals("ユーザマスタ概要", actual.getTableNames()[2]);
            Assertions.assertEquals("業務ドメイン", actual.getTableNames()[3]);
        }

        @Test
        public void testFromXlsWithSchemaToCsv() throws Exception {
            Convert.main(this.getArgs("/paramConvertXlsWithSchemaToCsv.txt"));
            final File src = new File(this.getBaseDir() + "/convert/xlswithschema2csv/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSource(DataSourceType.csv)
                                    .setSrc(src)
                                    .setEncoding("Shift-Jis")
                                    .build()));
            Assertions.assertEquals(4, actual.getTableNames().length);
            Assertions.assertEquals("テーブル一覧", actual.getTableNames()[0]);
            Assertions.assertEquals("ユーザマスタ", actual.getTableNames()[1]);
            Assertions.assertEquals("ユーザマスタ概要", actual.getTableNames()[2]);
            Assertions.assertEquals("業務ドメイン", actual.getTableNames()[3]);
        }

        @Test
        public void testTableNameMap() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMap.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/result/paramConvertMultiCsvTableNameMap.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("1", merged.getValue(0, "key"));
            Assertions.assertEquals("2", merged.getValue(0, "columna"));
            Assertions.assertEquals("2", merged.getValue(1, "key"));
            Assertions.assertEquals("test", merged.getValue(1, "columnb"));
            Assertions.assertEquals("3", merged.getValue(2, "key"));
            Assertions.assertEquals("10", merged.getValue(3, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(3, "columnc"));
            Assertions.assertEquals("20", merged.getValue(4, "key"));
            Assertions.assertEquals("30", merged.getValue(5, "key"));
        }

        @Test
        public void testTableNameMapNoSortToCsv() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMapNoSortToCsv.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/NoSortCsv/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.csv)
                                    .setEncoding("UTF-8")
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("10", merged.getValue(0, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(0, "columnc"));
            Assertions.assertEquals("20", merged.getValue(1, "key"));
            Assertions.assertEquals("30", merged.getValue(2, "key"));
            Assertions.assertEquals("1", merged.getValue(3, "key"));
            Assertions.assertEquals("2", merged.getValue(3, "columna"));
            Assertions.assertEquals("2", merged.getValue(4, "key"));
            Assertions.assertEquals("test", merged.getValue(4, "columnb"));
            Assertions.assertEquals("3", merged.getValue(5, "key"));
        }

        @Test
        public void testTableNameMapNoSortToXls() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMapNoSortToXls.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/NoSortXls/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xls)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("10", merged.getValue(0, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(0, "columnc"));
            Assertions.assertEquals("20", merged.getValue(1, "key"));
            Assertions.assertEquals("30", merged.getValue(2, "key"));
            Assertions.assertEquals("1", merged.getValue(3, "key"));
            Assertions.assertEquals("2", merged.getValue(3, "columna"));
            Assertions.assertEquals("2", merged.getValue(4, "key"));
            Assertions.assertEquals("test", merged.getValue(4, "columnb"));
            Assertions.assertEquals("3", merged.getValue(5, "key"));
        }

        @Test
        public void testTableNameMapNoSortToXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMapNoSortToXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/NoSortXlsx/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("10", merged.getValue(0, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(0, "columnc"));
            Assertions.assertEquals("20", merged.getValue(1, "key"));
            Assertions.assertEquals("30", merged.getValue(2, "key"));
            Assertions.assertEquals("1", merged.getValue(3, "key"));
            Assertions.assertEquals("2", merged.getValue(3, "columna"));
            Assertions.assertEquals("2", merged.getValue(4, "key"));
            Assertions.assertEquals("test", merged.getValue(4, "columnb"));
            Assertions.assertEquals("3", merged.getValue(5, "key"));
        }

        @Test
        public void testTableNameMapNoSortToXlsPerTable() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMapNoSortToXlsPerTable.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/NoSortXlsBook/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xls)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("10", merged.getValue(0, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(0, "columnc"));
            Assertions.assertEquals("20", merged.getValue(1, "key"));
            Assertions.assertEquals("30", merged.getValue(2, "key"));
            Assertions.assertEquals("1", merged.getValue(3, "key"));
            Assertions.assertEquals("2", merged.getValue(3, "columna"));
            Assertions.assertEquals("2", merged.getValue(4, "key"));
            Assertions.assertEquals("test", merged.getValue(4, "columnb"));
            Assertions.assertEquals("3", merged.getValue(5, "key"));
        }

        @Test
        public void testTableNameMapNoSortToXlsxPerTable() throws Exception {
            Convert.main(this.getArgs("/paramConvertMultiCsvTableNameMapNoSortToXlsxPerTable.txt"));
            final File src = new File(this.getBaseDir() + "/convert/tablenamemap/NoSortXlsxBook/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            Assertions.assertEquals(1, actual.getTableNames().length);
            final ComparableTable merged = actual.getTable("merge");
            Assertions.assertEquals(4, merged.getNumberOfColumns());
            Assertions.assertEquals(6, merged.getRowCount());
            Assertions.assertEquals("10", merged.getValue(0, "key"));
            Assertions.assertEquals("column3:4", merged.getValue(0, "columnc"));
            Assertions.assertEquals("20", merged.getValue(1, "key"));
            Assertions.assertEquals("30", merged.getValue(2, "key"));
            Assertions.assertEquals("1", merged.getValue(3, "key"));
            Assertions.assertEquals("2", merged.getValue(3, "columna"));
            Assertions.assertEquals("2", merged.getValue(4, "key"));
            Assertions.assertEquals("test", merged.getValue(4, "columnb"));
            Assertions.assertEquals("3", merged.getValue(5, "key"));
        }

        @Test
        public void testFromCsvToSplitMultiXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvSplitMultiXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/split/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            Assertions.assertEquals(8, actual.getTableNames().length);
            final ITable split1 = actual.getTables()[0];
            Assertions.assertEquals("0000_multi1", split1.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, ConvertTest.getColumnNames(split1));
            Assertions.assertEquals(2, split1.getRowCount());
            Assertions.assertEquals("1", split1.getValue(0, "key"));
            Assertions.assertEquals("2", split1.getValue(1, "key"));
            final ITable split2 = actual.getTables()[1];
            Assertions.assertEquals("0000_multi2", split2.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, ConvertTest.getColumnNames(split2));
            Assertions.assertEquals(2, split2.getRowCount());
            Assertions.assertEquals("1", split2.getValue(0, "key"));
            Assertions.assertEquals("2", split2.getValue(1, "key"));
            final ITable split3 = actual.getTables()[2];
            Assertions.assertEquals("0000_rename", split3.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, ConvertTest.getColumnNames(split3));
            Assertions.assertEquals(2, split3.getRowCount());
            Assertions.assertEquals("1", split3.getValue(0, "key"));
            Assertions.assertEquals("2", split3.getValue(1, "key"));
            final ITable split4 = actual.getTables()[3];
            Assertions.assertEquals("0001_rename", split4.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, ConvertTest.getColumnNames(split4));
            Assertions.assertEquals(1, split4.getRowCount());
            Assertions.assertEquals("3", split4.getValue(0, "key"));
            final ITable split5 = actual.getTables()[4];
            Assertions.assertEquals("multi1_00", split5.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, ConvertTest.getColumnNames(split5));
            Assertions.assertEquals(1, split5.getRowCount());
            Assertions.assertEquals("2", split5.getValue(0, "key"));
            final ITable split6 = actual.getTables()[5];
            Assertions.assertEquals("multi1_01", split6.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, ConvertTest.getColumnNames(split6));
            Assertions.assertEquals(1, split6.getRowCount());
            Assertions.assertEquals("3", split6.getValue(0, "key"));
            final ITable split7 = actual.getTables()[6];
            Assertions.assertEquals("multi2_00", split7.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, ConvertTest.getColumnNames(split7));
            Assertions.assertEquals(1, split7.getRowCount());
            Assertions.assertEquals("2", split7.getValue(0, "key"));
            final ITable split8 = actual.getTables()[7];
            Assertions.assertEquals("multi2_01", split8.getTableMetaData().getTableName());
            Assertions.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, ConvertTest.getColumnNames(split8));
            Assertions.assertEquals(1, split8.getRowCount());
            Assertions.assertEquals("3", split8.getValue(0, "key"));
        }

        @Test
        public void testFromCsvToSplitByColumnMultiXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvSplitByColumnMultiXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/split/keysplit_result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            Assertions.assertEquals(3, actual.getTableNames().length);
            final ITable split1 = actual.getTables()[0];
            Assertions.assertEquals("0000_break_key1", split1.getTableMetaData().getTableName());
            Assertions.assertEquals(4, split1.getRowCount());
            Assertions.assertEquals("1", split1.getValue(0, "key1"));
            Assertions.assertEquals("4", split1.getValue(0, "key2"));
            Assertions.assertEquals("number", split1.getValue(0, "col1"));
            Assertions.assertEquals("2", split1.getValue(1, "key1"));
            Assertions.assertEquals("3", split1.getValue(1, "key2"));
            Assertions.assertEquals("number", split1.getValue(1, "col1"));
            Assertions.assertEquals("A", split1.getValue(2, "key1"));
            Assertions.assertEquals("1", split1.getValue(2, "key2"));
            Assertions.assertEquals("test", split1.getValue(2, "col1"));
            Assertions.assertEquals("A", split1.getValue(3, "key1"));
            Assertions.assertEquals("2", split1.getValue(3, "key2"));
            Assertions.assertEquals("test", split1.getValue(3, "col1"));
            final ITable split2 = actual.getTables()[1];
            Assertions.assertEquals("0001_break_key1", split2.getTableMetaData().getTableName());
            Assertions.assertEquals("A", split2.getValue(0, "key1"));
            Assertions.assertEquals("3", split2.getValue(0, "key2"));
            Assertions.assertEquals("test3", split2.getValue(0, "col1"));
            Assertions.assertEquals("B", split2.getValue(1, "key1"));
            Assertions.assertEquals("3", split2.getValue(1, "key2"));
            Assertions.assertEquals("", split2.getValue(1, "col1"));
            Assertions.assertEquals("B", split2.getValue(2, "key1"));
            Assertions.assertEquals("4", split2.getValue(2, "key2"));
            Assertions.assertEquals("", split2.getValue(2, "col1"));
            Assertions.assertEquals("C", split2.getValue(3, "key1"));
            Assertions.assertEquals("10", split2.getValue(3, "key2"));
            Assertions.assertEquals("", split2.getValue(3, "col1"));
            final ITable split3 = actual.getTables()[2];
            Assertions.assertEquals("0002_break_key1", split3.getTableMetaData().getTableName());
            Assertions.assertEquals(1, split3.getRowCount());
            Assertions.assertEquals("あ", split3.getValue(0, "key1"));
            Assertions.assertEquals("3", split3.getValue(0, "key2"));
            Assertions.assertEquals("", split3.getValue(0, "col1"));
        }

        @Test
        public void testFromCsvToSeparateMultiXlsx() throws Exception {
            Convert.main(this.getArgs("/paramConvertCsvSeparateMultiXlsx.txt"));
            final File src = new File(this.getBaseDir() + "/convert/separate/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            Assertions.assertEquals(3, actual.getTableNames().length);
            Assertions.assertEquals("multi2", actual.getTableNames()[0]);
            final ITable split1 = actual.getTables()[1];
            Assertions.assertEquals("split1", split1.getTableMetaData().getTableName());
            Assertions.assertEquals(2, split1.getRowCount());
            Assertions.assertEquals("1", split1.getValue(0, "key"));
            Assertions.assertEquals("2", split1.getValue(1, "key"));
            final ITable split2 = actual.getTables()[2];
            Assertions.assertEquals("split2", split2.getTableMetaData().getTableName());
            Assertions.assertEquals(2, split2.getRowCount());
            Assertions.assertEquals("2", split2.getValue(0, "key"));
            Assertions.assertEquals("3", split2.getValue(1, "key"));
        }

        @Test
        public void testFromFilePathToCsv() throws Exception {
            Convert.main(new String[]{"@" + ConvertTest.testResourceDir + "/paramConvertFilePathToCsv.txt"
                    , "-result=" + this.getBaseDir() + "/convert/file2csv/recursive/result"
                    , "-recursive=true"});
            final File src = new File(this.getBaseDir() + "/convert/file2csv/recursive/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.csv)
                                    .setEncoding("UTF-8")
                                    .build()));
            final ITable result = actual.getTables()[0];
            Assertions.assertEquals(4, result.getRowCount());
            Assertions.assertEquals(Set.of("csv.csv", "test.txt", "sub.csv", "sub.txt"),
                    Set.of(result.getValue(0, "NAME")
                            , result.getValue(1, "NAME")
                            , result.getValue(2, "NAME")
                            , result.getValue(3, "NAME")
                    ));
        }

        @Test
        public void testFromFilePathToCsvNotRecursive() throws Exception {
            Convert.main(new String[]{"@" + ConvertTest.testResourceDir + "/paramConvertFilePathToCsv.txt"
                    , "-result=" + this.getBaseDir() + "/convert/file2csv/no-recursive/result"
                    , "-recursive=false"});
            final File src = new File(this.getBaseDir() + "/convert/file2csv/no-recursive/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.csv)
                                    .setEncoding("UTF-8")
                                    .build()));
            final ITable result = actual.getTables()[0];
            Assertions.assertEquals(2, result.getRowCount());
            Assertions.assertEquals(Set.of("csv.csv", "test.txt"),
                    Set.of(result.getValue(0, "NAME")
                            , result.getValue(1, "NAME")
                    ));
        }

        @Test
        public void testFromFilePathToCsvFilterExtension() throws Exception {
            Convert.main(new String[]{"@" + ConvertTest.testResourceDir + "/paramConvertFilePathToCsv.txt"
                    , "-result=" + this.getBaseDir() + "/convert/file2csv/no-recursive/only-txt/result"
                    , "-recursive=true", "-extension=txt"});
            final File src = new File(this.getBaseDir() + "/convert/file2csv/no-recursive/only-txt/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.csv)
                                    .setEncoding("UTF-8")
                                    .build()));
            final ITable result = actual.getTables()[0];
            Assertions.assertEquals(2, result.getRowCount());
            Assertions.assertEquals(Set.of("test.txt", "sub.txt"),
                    Set.of(result.getValue(0, "NAME")
                            , result.getValue(1, "NAME")
                    ));
        }

        @Test
        public void testFromFilePathToCsvWithSetting() throws Exception {
            Files.writeString(new File(this.getBaseDir(), "testConvertFilePathToCsvWithSetting.json").toPath(), """
                    {
                      "commonSettings":[
                      {
                      "string":{"NO_EXTENSION":"get('NAME').substring(0,get('NAME').lastIndexOf('.'))","RELATIVE_PATH":"get('RELATIVE_PATH').replace('\\\\\\\\','/')"},
                      "filter":["NAME =$ '.txt'","RELATIVE_PATH !~ '.*/.*'"]
                      }
                      ]
                    }
                    """);
            Convert.main(new String[]{"@" + ConvertTest.testResourceDir + "/paramConvertFilePathToCsv.txt"
                    , "-result=" + this.getBaseDir() + "/convert/file2csv/customize/result"
                    , "-setting=" + this.getBaseDir() + "/testConvertFilePathToCsvWithSetting.json"});
            final File src = new File(this.getBaseDir() + "/convert/file2csv/customize/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableCsvDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.csv)
                                    .setEncoding("UTF-8")
                                    .build()));
            final ITable result = actual.getTables()[0];
            Assertions.assertEquals(1, result.getRowCount());
            Assertions.assertEquals("test", result.getValue(0, "NO_EXTENSION"));
        }

        @Test
        public void testDistinctColumn() throws Exception {
            Convert.main(this.getArgs("/paramConvertDistinctColumn.txt"));
            final File src = new File(this.getBaseDir() + "/convert/distinct/result");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ITable result = actual.getTable("combikey");
            Assertions.assertEquals(7, result.getRowCount());
            Assertions.assertEquals("A", result.getValue(0, "key1"));
            Assertions.assertEquals("test", result.getValue(0, "col1"));
            Assertions.assertEquals("C", result.getValue(1, "key1"));
            Assertions.assertEquals("", result.getValue(1, "col1"));
            Assertions.assertEquals("A", result.getValue(2, "key1"));
            Assertions.assertEquals("test3", result.getValue(2, "col1"));
            Assertions.assertEquals("B", result.getValue(3, "key1"));
            Assertions.assertEquals("", result.getValue(3, "col1"));
            Assertions.assertEquals("あ", result.getValue(4, "key1"));
            Assertions.assertEquals("", result.getValue(4, "col1"));
            Assertions.assertEquals("1", result.getValue(5, "key1"));
            Assertions.assertEquals("number", result.getValue(5, "col1"));
            Assertions.assertEquals("2", result.getValue(6, "key1"));
            Assertions.assertEquals("number", result.getValue(6, "col1"));
        }

        @Test
        public void testDistinctWithTableNameMap() throws Exception {
            Convert.main(this.getArgs("/paramConvertDistinctWithTableNameMap.txt"));
            final File src = new File(this.getBaseDir() + "/convert/distinct/resultrename");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ITable result = actual.getTable("distinct");
            Assertions.assertEquals(7, result.getRowCount());
            Assertions.assertEquals("A", result.getValue(0, "key1"));
            Assertions.assertEquals("test", result.getValue(0, "col1"));
            Assertions.assertEquals("C", result.getValue(1, "key1"));
            Assertions.assertEquals("", result.getValue(1, "col1"));
            Assertions.assertEquals("A", result.getValue(2, "key1"));
            Assertions.assertEquals("test3", result.getValue(2, "col1"));
            Assertions.assertEquals("B", result.getValue(3, "key1"));
            Assertions.assertEquals("", result.getValue(3, "col1"));
            Assertions.assertEquals("あ", result.getValue(4, "key1"));
            Assertions.assertEquals("", result.getValue(4, "col1"));
            Assertions.assertEquals("1", result.getValue(5, "key1"));
            Assertions.assertEquals("number", result.getValue(5, "col1"));
            Assertions.assertEquals("2", result.getValue(6, "key1"));
            Assertions.assertEquals("number", result.getValue(6, "col1"));
        }

        @Test
        public void testDistinctWithMerge() throws Exception {
            Convert.main(this.getArgs("/paramConvertDistinctWithMerge.txt"));
            final File src = new File(this.getBaseDir() + "/convert/distinct/resultmerge");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ITable result = actual.getTable("merge");
            Assertions.assertEquals(4, result.getRowCount());
            Assertions.assertEquals("2", result.getValue(0, "key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "columna"));
            Assertions.assertEquals("3", result.getValue(1, "key"));
            Assertions.assertEquals("", result.getValue(1, "columna"));
            Assertions.assertEquals("10", result.getValue(2, "key"));
            Assertions.assertEquals("column1:2", result.getValue(2, "columna"));
            Assertions.assertEquals("30", result.getValue(3, "key"));
            Assertions.assertEquals("column1:", result.getValue(3, "columna"));
        }

        @Test
        public void testOuterJoin() throws Exception {
            Convert.main(this.getArgs("/paramConvertOuterJoin.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertOuterJoin.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("multi1_with_merge");
            Assertions.assertEquals(3, result.getRowCount());
            Assertions.assertEquals(8, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "multi1_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "multi1_columna"));
            Assertions.assertEquals("test", result.getValue(0, "multi1_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(0, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(0, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "merge_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "multi1_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "merge_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "merge_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "merge_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "merge_columnc"));
            Assertions.assertEquals("30", result.getValue(2, "multi1_key"));
            Assertions.assertEquals("column1:", result.getValue(2, "multi1_columna"));
            Assertions.assertEquals("column2:", result.getValue(2, "multi1_columnb"));
            Assertions.assertEquals("column3:", result.getValue(2, "multi1_columnc"));
            Assertions.assertEquals("", result.getValue(2, "merge_key"));
            Assertions.assertEquals("", result.getValue(2, "merge_columna"));
            Assertions.assertEquals("", result.getValue(2, "merge_columnb"));
            Assertions.assertEquals("", result.getValue(2, "merge_columnc"));
        }

        @Test
        public void testFullJoin() throws Exception {
            Convert.main(this.getArgs("/paramConvertFullJoin.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertFullJoin.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("multi1_with_merge");
            Assertions.assertEquals(4, result.getRowCount());
            Assertions.assertEquals(8, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "multi1_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "multi1_columna"));
            Assertions.assertEquals("test", result.getValue(0, "multi1_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(0, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(0, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "merge_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "multi1_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "merge_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "merge_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "merge_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "merge_columnc"));
            Assertions.assertEquals("30", result.getValue(2, "multi1_key"));
            Assertions.assertEquals("column1:", result.getValue(2, "multi1_columna"));
            Assertions.assertEquals("column2:", result.getValue(2, "multi1_columnb"));
            Assertions.assertEquals("column3:", result.getValue(2, "multi1_columnc"));
            Assertions.assertEquals("", result.getValue(2, "merge_key"));
            Assertions.assertEquals("", result.getValue(2, "merge_columna"));
            Assertions.assertEquals("", result.getValue(2, "merge_columnb"));
            Assertions.assertEquals("", result.getValue(2, "merge_columnc"));
            Assertions.assertEquals("", result.getValue(3, "multi1_key"));
            Assertions.assertEquals("", result.getValue(3, "multi1_columna"));
            Assertions.assertEquals("", result.getValue(3, "multi1_columnb"));
            Assertions.assertEquals("", result.getValue(3, "multi1_columnc"));
            Assertions.assertEquals("3", result.getValue(3, "merge_key"));
            Assertions.assertEquals("", result.getValue(3, "merge_columna"));
            Assertions.assertEquals("", result.getValue(3, "merge_columnb"));
            Assertions.assertEquals("column3:", result.getValue(3, "merge_columnc"));
        }

        @Test
        public void testInnerJoin() throws Exception {
            Convert.main(this.getArgs("/paramConvertInnerJoin.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertInnerJoin.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("multi1_with_merge");
            Assertions.assertEquals(2, result.getRowCount());
            Assertions.assertEquals(8, result.getNumberOfColumns());
            Assertions.assertEquals("10", result.getValue(0, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(0, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(0, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(0, "multi1_columnc"));
            Assertions.assertEquals("10", result.getValue(0, "merge_key"));
            Assertions.assertEquals("column1:2", result.getValue(0, "merge_columna"));
            Assertions.assertEquals("column2:3", result.getValue(0, "merge_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(0, "merge_columnc"));
            Assertions.assertEquals("2", result.getValue(1, "multi1_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(1, "multi1_columna"));
            Assertions.assertEquals("test", result.getValue(1, "multi1_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(1, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(1, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(1, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(1, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(1, "merge_columnc"));
        }

        @Test
        public void testInnerJoinByExpression() throws Exception {
            Convert.main(this.getArgs("/paramConvertInnerJoinByExpression.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertInnerJoinByExpression.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("multi1_with_merge");
            Assertions.assertEquals(5, result.getRowCount());
            Assertions.assertEquals(8, result.getNumberOfColumns());
            Assertions.assertEquals("10", result.getValue(0, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(0, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(0, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(0, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(0, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(0, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "merge_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "multi1_columnc"));
            Assertions.assertEquals("3", result.getValue(1, "merge_key"));
            Assertions.assertEquals("", result.getValue(1, "merge_columna"));
            Assertions.assertEquals("", result.getValue(1, "merge_columnb"));
            Assertions.assertEquals("column3:", result.getValue(1, "merge_columnc"));
            Assertions.assertEquals("30", result.getValue(2, "multi1_key"));
            Assertions.assertEquals("column1:", result.getValue(2, "multi1_columna"));
            Assertions.assertEquals("column2:", result.getValue(2, "multi1_columnb"));
            Assertions.assertEquals("column3:", result.getValue(2, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(2, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(2, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(2, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(2, "merge_columnc"));
            Assertions.assertEquals("30", result.getValue(3, "multi1_key"));
            Assertions.assertEquals("column1:", result.getValue(3, "multi1_columna"));
            Assertions.assertEquals("column2:", result.getValue(3, "multi1_columnb"));
            Assertions.assertEquals("column3:", result.getValue(3, "multi1_columnc"));
            Assertions.assertEquals("3", result.getValue(3, "merge_key"));
            Assertions.assertEquals("", result.getValue(3, "merge_columna"));
            Assertions.assertEquals("", result.getValue(3, "merge_columnb"));
            Assertions.assertEquals("column3:", result.getValue(3, "merge_columnc"));
            Assertions.assertEquals("30", result.getValue(4, "multi1_key"));
            Assertions.assertEquals("column1:", result.getValue(4, "multi1_columna"));
            Assertions.assertEquals("column2:", result.getValue(4, "multi1_columnb"));
            Assertions.assertEquals("column3:", result.getValue(4, "multi1_columnc"));
            Assertions.assertEquals("10", result.getValue(4, "merge_key"));
            Assertions.assertEquals("column1:2", result.getValue(4, "merge_columna"));
            Assertions.assertEquals("column2:3", result.getValue(4, "merge_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(4, "merge_columnc"));
        }

        @Test
        public void testInnerJoinWithSplit() throws Exception {
            Convert.main(this.getArgs("/paramConvertInnerJoinWithSplit.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertInnerJoinWithSplit.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            ComparableTable result = actual.getTable("split_00");
            Assertions.assertEquals(1, result.getRowCount());
            Assertions.assertEquals(4, result.getNumberOfColumns());
            Assertions.assertEquals("10", result.getValue(0, "key"));
            Assertions.assertEquals("column1:2column2:3", result.getValue(0, "columnab"));
            Assertions.assertEquals("column2:3column3:4", result.getValue(0, "columnbc"));
            Assertions.assertEquals("column3:4column1:2", result.getValue(0, "columnca"));
            result = actual.getTable("split_01");
            Assertions.assertEquals(1, result.getRowCount());
            Assertions.assertEquals(4, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "key"));
            Assertions.assertEquals("あ\nいうえおtest", result.getValue(0, "columnab"));
            Assertions.assertEquals("testcolumn3:5", result.getValue(0, "columnbc"));
            Assertions.assertEquals("column3:5あ\nいうえお", result.getValue(0, "columnca"));
        }

        @Test
        public void testInnerJoinWithSeparate() throws Exception {
            Convert.main(this.getArgs("/paramConvertInnerJoinWithSeparate.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertInnerJoinWithSeparate.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            ComparableTable result = actual.getTable("separate");
            Assertions.assertEquals(3, result.getRowCount());
            Assertions.assertEquals(1, result.getNumberOfColumns());
            Assertions.assertEquals("0", result.getValue(0, "merge_key"));
            Assertions.assertEquals("2", result.getValue(1, "merge_key"));
            Assertions.assertEquals("10", result.getValue(2, "merge_key"));
            result = actual.getTable("over1");
            Assertions.assertEquals(3, result.getRowCount());
            Assertions.assertEquals(8, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "multi1_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "multi1_columna"));
            Assertions.assertEquals("test", result.getValue(0, "multi1_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "multi1_columnc"));
            Assertions.assertEquals("2", result.getValue(0, "merge_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "merge_columna"));
            Assertions.assertEquals("test", result.getValue(0, "merge_columnb"));
            Assertions.assertEquals("column3:5", result.getValue(0, "merge_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "multi1_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "multi1_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "multi1_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "multi1_columnc"));
            Assertions.assertEquals("10", result.getValue(1, "merge_key"));
            Assertions.assertEquals("column1:2", result.getValue(1, "merge_columna"));
            Assertions.assertEquals("column2:3", result.getValue(1, "merge_columnb"));
            Assertions.assertEquals("column3:4", result.getValue(1, "merge_columnc"));
            Assertions.assertEquals("", result.getValue(2, "multi1_key"));
            Assertions.assertEquals("", result.getValue(2, "multi1_columna"));
            Assertions.assertEquals("", result.getValue(2, "multi1_columnb"));
            Assertions.assertEquals("", result.getValue(2, "multi1_columnc"));
            Assertions.assertEquals("3", result.getValue(2, "merge_key"));
            Assertions.assertEquals("", result.getValue(2, "merge_columna"));
            Assertions.assertEquals("", result.getValue(2, "merge_columnb"));
            Assertions.assertEquals("column3:", result.getValue(2, "merge_columnc"));
        }

        @Test
        public void testInnerJoinWithRename() throws Exception {
            Convert.main(this.getArgs("/paramConvertInnerJoinWithRename.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertInnerJoinWithRename.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            ComparableTable result = actual.getTable("under3");
            Assertions.assertEquals(1, result.getRowCount());
            Assertions.assertEquals(4, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "key"));
            Assertions.assertEquals("あ\nいうえおtest", result.getValue(0, "columnab"));
            Assertions.assertEquals("testcolumn3:5", result.getValue(0, "columnbc"));
            Assertions.assertEquals("column3:5あ\nいうえお", result.getValue(0, "columnca"));
            result = actual.getTable("over1");
            Assertions.assertEquals(2, result.getRowCount());
            Assertions.assertEquals(4, result.getNumberOfColumns());
            Assertions.assertEquals("10", result.getValue(0, "key"));
            Assertions.assertEquals("column1:2column2:3", result.getValue(0, "columnab"));
            Assertions.assertEquals("column2:3column3:4", result.getValue(0, "columnbc"));
            Assertions.assertEquals("column3:4column1:2", result.getValue(0, "columnca"));
            Assertions.assertEquals("2", result.getValue(1, "key"));
            Assertions.assertEquals("あ\nいうえおtest", result.getValue(1, "columnab"));
            Assertions.assertEquals("testcolumn3:5", result.getValue(1, "columnbc"));
            Assertions.assertEquals("column3:5あ\nいうえお", result.getValue(1, "columnca"));
        }

        @Test
        public void testJoinMultiple() throws Exception {
            Convert.main(this.getArgs("/paramConvertJoinMultiple.txt"));
            final File src = new File(this.getBaseDir() + "/convert/join/result/paramConvertJoinMultiple.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("firstJoin_with_keyChange");
            Assertions.assertEquals(4, result.getRowCount());
            Assertions.assertEquals(12, result.getNumberOfColumns());
            Assertions.assertEquals("2", result.getValue(0, "firstJoin_multi1_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "firstJoin_multi1_column1"));
            Assertions.assertEquals("test", result.getValue(0, "firstJoin_multi1_column2"));
            Assertions.assertEquals("5", result.getValue(0, "firstJoin_multi1_column3"));
            Assertions.assertEquals("2", result.getValue(0, "firstJoin_multi2_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "firstJoin_multi2_columna"));
            Assertions.assertEquals("test", result.getValue(0, "firstJoin_multi2_columnb"));
            Assertions.assertEquals("5", result.getValue(0, "firstJoin_multi2_columnc"));
            Assertions.assertEquals("2", result.getValue(0, "keyChange_key"));
            Assertions.assertEquals("あ\nいうえお", result.getValue(0, "keyChange_column1"));
            Assertions.assertEquals("test", result.getValue(0, "keyChange_column2"));
            Assertions.assertEquals("5", result.getValue(0, "keyChange_column3"));
            Assertions.assertEquals("3", result.getValue(1, "firstJoin_multi1_key"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi1_column1"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi1_column2"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi1_column3"));
            Assertions.assertEquals("3", result.getValue(1, "firstJoin_multi2_key"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi2_columna"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi2_columnb"));
            Assertions.assertEquals("", result.getValue(1, "firstJoin_multi2_columnc"));
            Assertions.assertEquals("", result.getValue(1, "keyChange_key"));
            Assertions.assertEquals("", result.getValue(1, "keyChange_column1"));
            Assertions.assertEquals("", result.getValue(1, "keyChange_column2"));
            Assertions.assertEquals("", result.getValue(1, "keyChange_column3"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi1_key"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi1_column1"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi1_column2"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi1_column3"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi2_key"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi2_columna"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi2_columnb"));
            Assertions.assertEquals("", result.getValue(2, "firstJoin_multi2_columnc"));
            Assertions.assertEquals("10", result.getValue(2, "keyChange_key"));
            Assertions.assertEquals("2", result.getValue(2, "keyChange_column1"));
            Assertions.assertEquals("3", result.getValue(2, "keyChange_column2"));
            Assertions.assertEquals("4", result.getValue(2, "keyChange_column3"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi1_key"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi1_column1"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi1_column2"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi1_column3"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi2_key"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi2_columna"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi2_columnb"));
            Assertions.assertEquals("", result.getValue(3, "firstJoin_multi2_columnc"));
            Assertions.assertEquals("30", result.getValue(3, "keyChange_key"));
            Assertions.assertEquals("", result.getValue(3, "keyChange_column1"));
            Assertions.assertEquals("", result.getValue(3, "keyChange_column2"));
            Assertions.assertEquals("", result.getValue(3, "keyChange_column3"));
        }

        @Test
        public void testAddFileInfo() throws Exception {
            Convert.main(this.getArgs("/paramConvertXlsxWithSchemaAndFileInfoJoin.txt"));
            final File src = new File(this.getBaseDir() + "/convert/xlsxwithschema2xlsx/result/paramConvertXlsxWithSchemaAndFileInfoJoin.xlsx");
            final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                    new ComparableXlsxDataSetProducer(
                            ComparableDataSetParam.builder()
                                    .setSrc(src)
                                    .setSource(DataSourceType.xlsx)
                                    .build()));
            final ComparableTable result = actual.getTable("ユーザマスタ概要_with_ユーザマスタ");
            Assertions.assertEquals(5, result.getRowCount());
            Assertions.assertEquals(17, result.getNumberOfColumns());
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ概要_論理名称", "ユーザマスタ概要_物理名称", "ユーザマスタ概要_システムID", "ユーザマスタ概要_システム名称", "ユーザマスタ概要_改訂日", "ユーザマスタ概要_改訂者", "ユーザマスタ_No", "ユーザマスタ_論理名称", "ユーザマスタ_物理名称", "ユーザマスタ_データ型", "ユーザマスタ_桁数", "ユーザマスタ_初期値", "ユーザマスタ_PK", "ユーザマスタ_IDX1", "ユーザマスタ_IDX2", "ユーザマスタ_NN", "ユーザマスタ_備考"}, ConvertTest.getColumnNames(result));
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ", "MST_USER", "SUPER_FLEXIBLE_SYSTEM", "すごいシステム", "2020/01/01", "太郎", "1", "ユーザID", "ID", "NVARCHAR", "10", "", "1", "", "", "", ""}
                    , result.getRow(0));
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ", "MST_USER", "SUPER_FLEXIBLE_SYSTEM", "すごいシステム", "2020/01/01", "太郎", "2", "パスワード", "PASSWORD", "NVARCHAR", "8", "", "", "", "", "", ""}
                    , result.getRow(1));
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ", "MST_USER", "SUPER_FLEXIBLE_SYSTEM", "すごいシステム", "2020/01/01", "太郎", "3", "名称", "NAME", "NVARCHAR", "40", "", "", "", "", "○", ""}
                    , result.getRow(2));
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ", "MST_USER", "SUPER_FLEXIBLE_SYSTEM", "すごいシステム", "2020/01/01", "太郎", "4", "電話番号", "TEL", "DECIMAL", "11,0", "", "", "", "", "", ""}
                    , result.getRow(3));
            Assertions.assertArrayEquals(new String[]{"ユーザマスタ", "MST_USER", "SUPER_FLEXIBLE_SYSTEM", "すごいシステム", "2020/01/01", "太郎", "5", "メールアドレス", "MAIL", "NVARCHAR", "40", "", "", "", "", "", ""}
                    , result.getRow(4));
        }

        protected String[] getArgs(final String parameterFile) {
            return new String[]{"@" + ConvertTest.testResourceDir + parameterFile};
        }

        protected String getBaseDir() {
            return ConvertTest.baseDir;
        }

    }

    @Nested
    class NoSystemPropertyTest extends TestCase {
    }

    @Nested
    class AllSystemPropertyTest extends TestCase {

        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ConvertTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/convert/all/base");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/convert/all/result");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/convert/all/dataset");
            System.setProperties(newProperty);
            ConvertTest.clean("target/test-temp/convert/all");
            ConvertTest.copy("src/test/resources/yo/dbunitcli/application/settings", "target/test-temp/convert/all/base/src/test/resources/yo/dbunitcli/application/settings");
            ConvertTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/convert/all/dataset/src/test/resources/yo/dbunitcli/application/src");
        }

        @Override
        protected String getBaseDir() {
            return ConvertTest.baseDir.replaceAll("/target/", "/target/test-temp/convert/all/result/target/");
        }
    }

    @Nested
    class ChangeWorkSpaceTest extends TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ConvertTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "target/test-temp/convert/base");
            System.setProperties(newProperty);
            ConvertTest.clean("target/test-temp/convert/base");
            ConvertTest.copy("src/test/resources/yo/dbunitcli/application", "target/test-temp/convert/base/src/test/resources/yo/dbunitcli/application");
        }

        @Override
        protected String getBaseDir() {
            return ConvertTest.baseDir.replaceAll("/target/", "/target/test-temp/convert/base/target/");
        }
    }

    @Nested
    class ChangeResultBaseTest extends TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ConvertTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target/test-temp/convert/result");
            System.setProperties(newProperty);
            ConvertTest.clean("target/test-temp/convert/result");
        }

        @Override
        protected String getBaseDir() {
            return ConvertTest.baseDir.replaceAll("/target/", "/target/test-temp/convert/result/target/");
        }
    }

    @Nested
    class ChangeDataSetBaseTest extends TestCase {
        @BeforeAll
        static void backup() {
            final Properties newProperty = new Properties();
            newProperty.putAll(ConvertTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "target/test-temp/convert/dataset");
            System.setProperties(newProperty);
            ConvertTest.clean("target/test-temp/convert/dataset");
            ConvertTest.copy("src/test/resources/yo/dbunitcli/application/src", "target/test-temp/convert/dataset/src/test/resources/yo/dbunitcli/application/src");
        }

    }
}
