package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ComparableDirectoryDataSetProducerTest {

    @Test
    public void test(@TempDir final Path tempDir) throws DataSetException, IOException {
        // テスト用ディレクトリ構造を構築
        // yo/dbunitcli/ 配下でincludeに一致するもの: common, common/filter, dataset (3件)
        // yo/dbunitcli/application はexcludeで除外
        // other はincludeに不一致
        final File src = tempDir.resolve("testroot").toFile();
        Files.createDirectories(tempDir.resolve("testroot/yo/dbunitcli/common"));
        Files.createDirectories(tempDir.resolve("testroot/yo/dbunitcli/common/filter"));
        Files.createDirectories(tempDir.resolve("testroot/yo/dbunitcli/dataset"));
        Files.createDirectories(tempDir.resolve("testroot/yo/dbunitcli/application")); // 除外対象
        Files.createDirectories(tempDir.resolve("testroot/other"));                    // 非対象

        final ComparableDataSet actual = new ComparableDirectoryDataSetProducer(
                ComparableDataSetParam.builder()
                        .setSrc(src)
                        .setRegInclude("yo[/\\\\]+dbunitcli[/\\\\]+")
                        .setRegExclude("application")
                        .setRecursive(true)
                        .build()).loadDataSet();

        Assertions.assertEquals(src.getPath(), actual.src());
        Assertions.assertEquals(1, actual.getTables().length);
        final ITable table = actual.getTable("testroot");
        Assertions.assertEquals(3, table.getRowCount());
    }
}