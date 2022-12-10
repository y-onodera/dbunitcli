package yo.dbunitcli.application;

import org.dbunit.dataset.ITable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
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

public class ConvertTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public ExpectedSystemExit exit = ExpectedSystemExit.none();

    private String testResourceDir;

    private String baseDir;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(this.getClass().getResource(".").getPath(), "UTF-8");
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
        final File src = new File(this.baseDir + "/split/resulfilteringsplit");
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

}
