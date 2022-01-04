package yo.dbunitcli.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.ExpectedException;
import yo.dbunitcli.dataset.ComparableDataSetImpl;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
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
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromRegexToXlsx.txt"});
        File src = new File(this.baseDir + "/regex2xlsx/result/paramFromRegexToXlsx.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("dirty", actual.getTableNames()[0]);
    }

    @Test
    public void testNoSetting() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromRegexToXlsxNoSetting.txt"});
        File src = new File(this.baseDir + "/regex2xlsx/result/paramFromRegexToXlsxNoSetting.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("dirty", actual.getTableNames()[0]);
    }

    @Test
    public void testFromCsvToXlsx() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromCsvToXlsx.txt"});
        File src = new File(this.baseDir + "/csv2xlsx/result/paramFromCsvToXlsx.xlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
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
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromCsvToMultiXlsx.txt"});
        File src = new File(this.baseDir + "/csv2xlsx/resultmultixlsx");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsxDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(2, actual.getTableNames().length);
        Assert.assertEquals("multi1", actual.getTableNames()[0]);
        Assert.assertEquals("multi2", actual.getTableNames()[1]);
    }

    @Test
    public void testFromCsvToMultiXls() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromCsvToMultiXls.txt"});
        File src = new File(this.baseDir + "/csv2xlsx/resultmultixls");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableXlsDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .build()));
        Assert.assertEquals(2, actual.getTableNames().length);
        Assert.assertEquals("multi1", actual.getTableNames()[0]);
        Assert.assertEquals("multi2", actual.getTableNames()[1]);
    }

    @Test
    public void testParamFromFixedFileToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromFixedFileToCsv.txt"});
        File src = new File(this.baseDir + "/fixed2csv/result");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("UTF-8")
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("固定長ファイル", actual.getTableNames()[0]);
        ComparableTable table = actual.getTable("固定長ファイル");
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
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramFromCsvqToCsv.txt"});
        File src = new File(this.baseDir + "/csvq/result");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("Shift-Jis")
                                .build()));
        Assert.assertEquals(1, actual.getTableNames().length);
        Assert.assertEquals("joinQuery", actual.getTableNames()[0]);
    }

    @Test
    public void testXlsxWithSchemaToCsv() throws Exception {
        Convert.main(new String[]{"@" + this.testResourceDir + "/paramXlsxWithSchemaToCsv.txt"});
        File src = new File(this.baseDir + "/xlsxwithschema/result");
        ComparableDataSetImpl actual = new ComparableDataSetImpl(
                new ComparableCsvDataSetProducer(
                        ComparableDataSetParam.builder()
                                .setSrc(src)
                                .setEncoding("Shift-Jis")
                                .build()));
        Assert.assertEquals(4, actual.getTableNames().length);
        Assert.assertEquals("テーブル一覧", actual.getTableNames()[0]);
        Assert.assertEquals("ユーザマスタ", actual.getTableNames()[1]);
        Assert.assertEquals("ユーザマスタ概要", actual.getTableNames()[2]);
        Assert.assertEquals("業務ドメイン", actual.getTableNames()[3]);
    }
}
