package yo.dbunitcli.application;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class ConvertTest {

    private String testResourceDir;

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8);
        this.testResourceDir = this.baseDir.replace("target/test-classes", "src/test/resources");
    }

    @Test
    public void testFromRegexToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertRegexToXlsx.txt"});
        final File src = new File(this.baseDir + "/regex2xlsx/result/paramConvertRegexToXlsx.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("dirty", actual.getTableNames()[0]);
    }

    @Test
    public void testNoSetting() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertRegexToXlsxNoSetting.txt"});
        final File src = new File(this.baseDir + "/regex2xlsx/result/paramConvertRegexToXlsxNoSetting.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("dirty", actual.getTableNames()[0]);
    }

    @Test
    public void testFromCsvToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvToXlsx.txt"});
        final File src = new File(this.baseDir + "/csv2xlsx/result/paramConvertCsvToXlsx.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(2, actual.getTableNames().length);
        Assert.assertEquals("multi1", actual.getTableNames()[0]);
        Assert.assertEquals("multi2", actual.getTableNames()[1]);
    }

    @Test
    public void testFromCsvToMultiXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvToMultiXlsx.txt"});
        final File src = new File(this.baseDir + "/csv2xlsx/resultmultixlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.xlsx)
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(2, actual.getTableNames().length);
        Assert.assertEquals("multi1", actual.getTableNames()[0]);
        Assert.assertEquals("multi2", actual.getTableNames()[1]);
    }

    @Test
    public void testFromCsvToMultiXls() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvToMultiXls.txt"});
        final File src = new File(this.baseDir + "/csv2xlsx/resultmultixls");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.xls)
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(2, actual.getTableNames().length);
        Assert.assertEquals("multi1", actual.getTableNames()[0]);
        Assert.assertEquals("multi2", actual.getTableNames()[1]);
    }

    @Test
    public void testparamConvertFixedFileToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertFixedFileToCsv.txt"});
        final File src = new File(this.baseDir + "/fixed2csv/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("UTF-8")
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("固定長ファイル", actual.getTableNames()[0]);
        final ComparableTable table = actual.getTable("固定長ファイル");
        Assert.assertEquals(4, table.getRowCount());
        Assert.assertEquals("a1a", table.getValue(0, "半角"));
        Assert.assertEquals("a  ", table.getValue(1, "半角"));
        Assert.assertEquals("   ", table.getValue(2, "半角"));
        Assert.assertEquals("   ", table.getValue(3, "半角"));
        Assert.assertEquals("123                                               ", table.getValue(0, "数値"));
        Assert.assertEquals("                                                  ", table.getValue(1, "数値"));
        Assert.assertEquals("123                                               ", table.getValue(2, "数値"));
        Assert.assertEquals("                                                  ", table.getValue(3, "数値"));
        Assert.assertEquals("あさぼらけ", table.getValue(0, "全角"));
        Assert.assertEquals("      有明の", table.getValue(1, "全角"));
        Assert.assertEquals("              1", table.getValue(2, "全角"));
        Assert.assertEquals("         月と", table.getValue(3, "全角"));
    }

    @Test
    public void testFromCsvqToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvqToCsv.txt"});
        final File src = new File(this.baseDir + "/csvq/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("Shift-Jis")
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("joinQuery", actual.getTableNames()[0]);
    }

    @Test
    public void testXlsxWithSchemaToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertXlsxWithSchemaToCsv.txt"});
        final File src = new File(this.baseDir + "/xlsxwithschema/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("Shift-Jis")
                                .build()));
        Assert.assertEquals(4, actual.getTableNames().length);
        Assert.assertEquals("テーブル一覧", actual.getTableNames()[0]);
        Assert.assertEquals("ユーザマスタ", actual.getTableNames()[1]);
        Assert.assertEquals("ユーザマスタ概要", actual.getTableNames()[2]);
        Assert.assertEquals("業務ドメイン", actual.getTableNames()[3]);
    }

    @Test
    public void testXlsWithSchemaToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertXlsWithSchemaToCsv.txt"});
        final File src = new File(this.baseDir + "/xlsxwithschema/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSource(DataSourceType.csv)
                                .setSrc(src)
                                .setEncoding("Shift-Jis")
                                .build()));
        Assert.assertEquals(4, actual.getTableNames().length);
        Assert.assertEquals("テーブル一覧", actual.getTableNames()[0]);
        Assert.assertEquals("ユーザマスタ", actual.getTableNames()[1]);
        Assert.assertEquals("ユーザマスタ概要", actual.getTableNames()[2]);
        Assert.assertEquals("業務ドメイン", actual.getTableNames()[3]);
    }

    @Test
    public void testTableNameMap() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMap.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/result/paramConvertMultiCsvTableNameMap.xlsx");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("1", merged.getValue(0, "key"));
        Assert.assertEquals("2", merged.getValue(0, "columna"));
        Assert.assertEquals("2", merged.getValue(1, "key"));
        Assert.assertEquals("test", merged.getValue(1, "columnb"));
        Assert.assertEquals("3", merged.getValue(2, "key"));
        Assert.assertEquals("10", merged.getValue(3, "key"));
        Assert.assertEquals("column3:4", merged.getValue(3, "columnc"));
        Assert.assertEquals("20", merged.getValue(4, "key"));
        Assert.assertEquals("30", merged.getValue(5, "key"));
    }

    @Test
    public void testTableNameMapNoSortToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMapNoSortToCsv.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/NoSortCsv/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF-8")
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("10", merged.getValue(0, "key"));
        Assert.assertEquals("column3:4", merged.getValue(0, "columnc"));
        Assert.assertEquals("20", merged.getValue(1, "key"));
        Assert.assertEquals("30", merged.getValue(2, "key"));
        Assert.assertEquals("1", merged.getValue(3, "key"));
        Assert.assertEquals("2", merged.getValue(3, "columna"));
        Assert.assertEquals("2", merged.getValue(4, "key"));
        Assert.assertEquals("test", merged.getValue(4, "columnb"));
        Assert.assertEquals("3", merged.getValue(5, "key"));
    }

    @Test
    public void testTableNameMapNoSortToXls() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMapNoSortToXls.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/NoSortXls/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xls)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("10", merged.getValue(0, "key"));
        Assert.assertEquals("column3:4", merged.getValue(0, "columnc"));
        Assert.assertEquals("20", merged.getValue(1, "key"));
        Assert.assertEquals("30", merged.getValue(2, "key"));
        Assert.assertEquals("1", merged.getValue(3, "key"));
        Assert.assertEquals("2", merged.getValue(3, "columna"));
        Assert.assertEquals("2", merged.getValue(4, "key"));
        Assert.assertEquals("test", merged.getValue(4, "columnb"));
        Assert.assertEquals("3", merged.getValue(5, "key"));
    }

    @Test
    public void testTableNameMapNoSortToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMapNoSortToXlsx.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/NoSortXlsx/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("10", merged.getValue(0, "key"));
        Assert.assertEquals("column3:4", merged.getValue(0, "columnc"));
        Assert.assertEquals("20", merged.getValue(1, "key"));
        Assert.assertEquals("30", merged.getValue(2, "key"));
        Assert.assertEquals("1", merged.getValue(3, "key"));
        Assert.assertEquals("2", merged.getValue(3, "columna"));
        Assert.assertEquals("2", merged.getValue(4, "key"));
        Assert.assertEquals("test", merged.getValue(4, "columnb"));
        Assert.assertEquals("3", merged.getValue(5, "key"));
    }

    @Test
    public void testTableNameMapNoSortToXlsPerTable() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMapNoSortToXlsPerTable.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/NoSortXlsBook/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xls)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("10", merged.getValue(0, "key"));
        Assert.assertEquals("column3:4", merged.getValue(0, "columnc"));
        Assert.assertEquals("20", merged.getValue(1, "key"));
        Assert.assertEquals("30", merged.getValue(2, "key"));
        Assert.assertEquals("1", merged.getValue(3, "key"));
        Assert.assertEquals("2", merged.getValue(3, "columna"));
        Assert.assertEquals("2", merged.getValue(4, "key"));
        Assert.assertEquals("test", merged.getValue(4, "columnb"));
        Assert.assertEquals("3", merged.getValue(5, "key"));
    }

    @Test
    public void testTableNameMapNoSortToXlsxPerTable() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertMultiCsvTableNameMapNoSortToXlsxPerTable.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/NoSortXlsxBook/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        final ITable merged = actual.getTables()[0];
        Assert.assertEquals("merge", merged.getTableMetaData().getTableName());
        Assert.assertEquals(4, merged.getTableMetaData().getColumns().length);
        Assert.assertEquals(6, merged.getRowCount());
        Assert.assertEquals("10", merged.getValue(0, "key"));
        Assert.assertEquals("column3:4", merged.getValue(0, "columnc"));
        Assert.assertEquals("20", merged.getValue(1, "key"));
        Assert.assertEquals("30", merged.getValue(2, "key"));
        Assert.assertEquals("1", merged.getValue(3, "key"));
        Assert.assertEquals("2", merged.getValue(3, "columna"));
        Assert.assertEquals("2", merged.getValue(4, "key"));
        Assert.assertEquals("test", merged.getValue(4, "columnb"));
        Assert.assertEquals("3", merged.getValue(5, "key"));
    }

    @Test
    public void testConvertCsvSplitMultiXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvSplitMultiXlsx.txt"});
        final File src = new File(this.baseDir + "/split/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        Assert.assertEquals(8, actual.getTableNames().length);
        final ITable split1 = actual.getTables()[0];
        Assert.assertEquals("0000_multi1", split1.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, getColumnNames(split1));
        Assert.assertEquals(2, split1.getRowCount());
        Assert.assertEquals("1", split1.getValue(0, "key"));
        Assert.assertEquals("2", split1.getValue(1, "key"));
        final ITable split2 = actual.getTables()[1];
        Assert.assertEquals("0000_multi2", split2.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, getColumnNames(split2));
        Assert.assertEquals(2, split2.getRowCount());
        Assert.assertEquals("1", split2.getValue(0, "key"));
        Assert.assertEquals("2", split2.getValue(1, "key"));
        final ITable split3 = actual.getTables()[2];
        Assert.assertEquals("0000_rename", split3.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, getColumnNames(split3));
        Assert.assertEquals(2, split3.getRowCount());
        Assert.assertEquals("1", split3.getValue(0, "key"));
        Assert.assertEquals("2", split3.getValue(1, "key"));
        final ITable split4 = actual.getTables()[3];
        Assert.assertEquals("0001_rename", split4.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, getColumnNames(split4));
        Assert.assertEquals(1, split4.getRowCount());
        Assert.assertEquals("3", split4.getValue(0, "key"));
        final ITable split5 = actual.getTables()[4];
        Assert.assertEquals("multi1_00", split5.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, getColumnNames(split5));
        Assert.assertEquals(1, split5.getRowCount());
        Assert.assertEquals("2", split5.getValue(0, "key"));
        final ITable split6 = actual.getTables()[5];
        Assert.assertEquals("multi1_01", split6.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "column1", "column2", "column3"}, getColumnNames(split6));
        Assert.assertEquals(1, split6.getRowCount());
        Assert.assertEquals("3", split6.getValue(0, "key"));
        final ITable split7 = actual.getTables()[6];
        Assert.assertEquals("multi2_00", split7.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, getColumnNames(split7));
        Assert.assertEquals(1, split7.getRowCount());
        Assert.assertEquals("2", split7.getValue(0, "key"));
        final ITable split8 = actual.getTables()[7];
        Assert.assertEquals("multi2_01", split8.getTableMetaData().getTableName());
        Assert.assertArrayEquals(new String[]{"key", "columna", "columnb", "columnc"}, getColumnNames(split8));
        Assert.assertEquals(1, split8.getRowCount());
        Assert.assertEquals("3", split8.getValue(0, "key"));
    }

    @Test
    public void testConvertCsvSplitByColumnMultiXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvSplitByColumnMultiXlsx.txt"});
        final File src = new File(this.baseDir + "/split/keysplit_result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        Assert.assertEquals(3, actual.getTableNames().length);
        final ITable split1 = actual.getTables()[0];
        Assert.assertEquals("0000_break_key1", split1.getTableMetaData().getTableName());
        Assert.assertEquals(4, split1.getRowCount());
        Assert.assertEquals("1", split1.getValue(0, "key1"));
        Assert.assertEquals("4", split1.getValue(0, "key2"));
        Assert.assertEquals("number", split1.getValue(0, "col1"));
        Assert.assertEquals("2", split1.getValue(1, "key1"));
        Assert.assertEquals("3", split1.getValue(1, "key2"));
        Assert.assertEquals("number", split1.getValue(1, "col1"));
        Assert.assertEquals("A", split1.getValue(2, "key1"));
        Assert.assertEquals("1", split1.getValue(2, "key2"));
        Assert.assertEquals("test", split1.getValue(2, "col1"));
        Assert.assertEquals("A", split1.getValue(3, "key1"));
        Assert.assertEquals("2", split1.getValue(3, "key2"));
        Assert.assertEquals("test", split1.getValue(3, "col1"));
        final ITable split2 = actual.getTables()[1];
        Assert.assertEquals("0001_break_key1", split2.getTableMetaData().getTableName());
        Assert.assertEquals("A", split2.getValue(0, "key1"));
        Assert.assertEquals("3", split2.getValue(0, "key2"));
        Assert.assertEquals("test3", split2.getValue(0, "col1"));
        Assert.assertEquals("B", split2.getValue(1, "key1"));
        Assert.assertEquals("3", split2.getValue(1, "key2"));
        Assert.assertEquals("", split2.getValue(1, "col1"));
        Assert.assertEquals("B", split2.getValue(2, "key1"));
        Assert.assertEquals("4", split2.getValue(2, "key2"));
        Assert.assertEquals("", split2.getValue(2, "col1"));
        Assert.assertEquals("C", split2.getValue(3, "key1"));
        Assert.assertEquals("10", split2.getValue(3, "key2"));
        Assert.assertEquals("", split2.getValue(3, "col1"));
        final ITable split3 = actual.getTables()[2];
        Assert.assertEquals("0002_break_key1", split3.getTableMetaData().getTableName());
        Assert.assertEquals(1, split3.getRowCount());
        Assert.assertEquals("あ", split3.getValue(0, "key1"));
        Assert.assertEquals("3", split3.getValue(0, "key2"));
        Assert.assertEquals("", split3.getValue(0, "col1"));
    }

    @Test
    public void testConvertCsvSeparateMultiXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertCsvSeparateMultiXlsx.txt"});
        final File src = new File(this.baseDir + "/separate/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        Assert.assertEquals(3, actual.getTableNames().length);
        Assert.assertEquals("multi2", actual.getTableNames()[0]);
        final ITable split1 = actual.getTables()[1];
        Assert.assertEquals("split1", split1.getTableMetaData().getTableName());
        Assert.assertEquals(2, split1.getRowCount());
        Assert.assertEquals("1", split1.getValue(0, "key"));
        Assert.assertEquals("2", split1.getValue(1, "key"));
        final ITable split2 = actual.getTables()[2];
        Assert.assertEquals("split2", split2.getTableMetaData().getTableName());
        Assert.assertEquals(2, split2.getRowCount());
        Assert.assertEquals("2", split2.getValue(0, "key"));
        Assert.assertEquals("3", split2.getValue(1, "key"));
    }

    @Test
    public void testConvertFilePathToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertFilePathToCsv.txt"});
        final File src = new File(this.baseDir + "/file/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF-8")
                                .build()));
        final ITable result = actual.getTables()[0];
        Assert.assertEquals(4, result.getRowCount());
        Assert.assertEquals(Set.of("csv.csv", "test.txt", "sub.csv", "sub.txt"),
                Set.of(result.getValue(0, "NAME")
                        , result.getValue(1, "NAME")
                        , result.getValue(2, "NAME")
                        , result.getValue(3, "NAME")
                ));
    }

    @Test
    public void testConvertFilePathToCsvNotRecursive() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertFilePathToCsv.txt", "-recursive=false"});
        final File src = new File(this.baseDir + "/file/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF-8")
                                .build()));
        final ITable result = actual.getTables()[0];
        Assert.assertEquals(2, result.getRowCount());
        Assert.assertEquals(Set.of("csv.csv", "test.txt"),
                Set.of(result.getValue(0, "NAME")
                        , result.getValue(1, "NAME")
                ));
    }

    @Test
    public void testConvertFilePathToCsvFilterExtension() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertFilePathToCsv.txt", "-extension=txt"});
        final File src = new File(this.baseDir + "/file/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF-8")
                                .build()));
        final ITable result = actual.getTables()[0];
        Assert.assertEquals(2, result.getRowCount());
        Assert.assertEquals(Set.of("test.txt", "sub.txt"),
                Set.of(result.getValue(0, "NAME")
                        , result.getValue(1, "NAME")
                ));
    }

    @Test
    public void testConvertFilePathToCsvWithSetting() throws Exception {
        Files.writeString(new File(this.baseDir, "testConvertFilePathToCsvWithSetting.json").toPath(), """
                {
                  "commonSettings":[
                  {
                  "string":{"NO_EXTENSION":"get('NAME').substring(0,get('NAME').lastIndexOf('.'))","RELATIVE_PATH":"get('RELATIVE_PATH').replace('\\\\\\\\','/')"},
                  "filter":["NAME =$ '.txt'","RELATIVE_PATH !~ '.*/.*'"]
                  }
                  ]
                }
                """);
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertFilePathToCsv.txt"
                , "-setting=" + this.baseDir + "/testConvertFilePathToCsvWithSetting.json"});
        final File src = new File(this.baseDir + "/file/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.csv)
                                .setEncoding("UTF-8")
                                .build()));
        final ITable result = actual.getTables()[0];
        Assert.assertEquals(1, result.getRowCount());
        Assert.assertEquals("test", result.getValue(0, "NO_EXTENSION"));
    }

    @Test
    public void testConvertDistinctColumn() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertDistinctColumn.txt"});
        final File src = new File(this.baseDir + "/setting/distinct/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        final ITable result = actual.getTable("combikey");
        Assert.assertEquals(7, result.getRowCount());
        Assert.assertEquals("A", result.getValue(0, "key1"));
        Assert.assertEquals("test", result.getValue(0, "col1"));
        Assert.assertEquals("C", result.getValue(1, "key1"));
        Assert.assertEquals("", result.getValue(1, "col1"));
        Assert.assertEquals("A", result.getValue(2, "key1"));
        Assert.assertEquals("test3", result.getValue(2, "col1"));
        Assert.assertEquals("B", result.getValue(3, "key1"));
        Assert.assertEquals("", result.getValue(3, "col1"));
        Assert.assertEquals("あ", result.getValue(4, "key1"));
        Assert.assertEquals("", result.getValue(4, "col1"));
        Assert.assertEquals("1", result.getValue(5, "key1"));
        Assert.assertEquals("number", result.getValue(5, "col1"));
        Assert.assertEquals("2", result.getValue(6, "key1"));
        Assert.assertEquals("number", result.getValue(6, "col1"));
    }

    @Test
    public void testConvertDistinctWithTableNameMap() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertDistinctWithTableNameMap.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/distinct/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        final ITable result = actual.getTable("distinct");
        Assert.assertEquals(7, result.getRowCount());
        Assert.assertEquals("A", result.getValue(0, "key1"));
        Assert.assertEquals("test", result.getValue(0, "col1"));
        Assert.assertEquals("C", result.getValue(1, "key1"));
        Assert.assertEquals("", result.getValue(1, "col1"));
        Assert.assertEquals("A", result.getValue(2, "key1"));
        Assert.assertEquals("test3", result.getValue(2, "col1"));
        Assert.assertEquals("B", result.getValue(3, "key1"));
        Assert.assertEquals("", result.getValue(3, "col1"));
        Assert.assertEquals("あ", result.getValue(4, "key1"));
        Assert.assertEquals("", result.getValue(4, "col1"));
        Assert.assertEquals("1", result.getValue(5, "key1"));
        Assert.assertEquals("number", result.getValue(5, "col1"));
        Assert.assertEquals("2", result.getValue(6, "key1"));
        Assert.assertEquals("number", result.getValue(6, "col1"));
    }

    @Test
    public void testConvertDistinctWithMerge() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramConvertDistinctWithMerge.txt"});
        final File src = new File(this.baseDir + "/tablenamemap/merge/distinct/result");
        final ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setSource(DataSourceType.xlsx)
                                .build()));
        final ITable result = actual.getTable("merge");
        Assert.assertEquals(4, result.getRowCount());
        Assert.assertEquals("2", result.getValue(0, "key"));
        Assert.assertEquals("あ\nいうえお", result.getValue(0, "columna"));
        Assert.assertEquals("3", result.getValue(1, "key"));
        Assert.assertEquals("", result.getValue(1, "columna"));
        Assert.assertEquals("10", result.getValue(2, "key"));
        Assert.assertEquals("column1:2", result.getValue(2, "columna"));
        Assert.assertEquals("30", result.getValue(3, "key"));
        Assert.assertEquals("column1:", result.getValue(3, "columna"));
    }

    private static String[] getColumnNames(final ITable split1) throws DataSetException {
        return Arrays.stream(split1.getTableMetaData().getColumns())
                .map(Column::getColumnName)
                .toArray(String[]::new);
    }

}
