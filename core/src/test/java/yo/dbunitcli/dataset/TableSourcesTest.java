package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

class TableSourcesTest {

    @TempDir
    Path src;

    @Test
    void settingで変えたテーブル名ごとに読込元を返す() throws IOException {
        this.writeSources();

        Assertions.assertEquals(Map.of(
                        "nested", List.of("sub/nested.csv"),
                        "renamed", List.of("big.csv"),
                        "small", List.of("small.csv")),
                this.tableSources("""
                        {"settings": [{"name": ["big"], "tableName": "renamed"}]}
                        """));
    }

    @Test
    void 複数のソースを1つのテーブルにまとめたら読込元をすべて返す() throws IOException {
        this.writeSources();

        Assertions.assertEquals(Map.of("merged", List.of("big.csv", "small.csv", "sub/nested.csv")),
                this.tableSources("""
                        {"settings": [{"pattern": {"regex": ".*"}, "tableName": "merged"}]}
                        """));
    }

    @Test
    void 分割したテーブルは分割前のテーブルの読込元を返す() throws IOException {
        this.writeSources();

        final Map<String, List<String>> actual = this.tableSources("""
                {"settings": [{"name": ["big"], "split": {"suffix": "_%02d", "limit": 1}}]}
                """);

        Assertions.assertEquals(List.of("big.csv"), actual.get("big_00"));
        Assertions.assertEquals(List.of("big.csv"), actual.get("big_01"));
    }

    @Test
    void joinの結果は結合したテーブルの読込元を返す() throws IOException {
        this.writeSources();

        final Map<String, List<String>> actual = this.tableSources("""
                {"settings": [{"innerJoin": {"left": "big", "right": "small", "column": ["key"]}, "tableName": "joined"}]}
                """);

        Assertions.assertEquals(List.of("big.csv", "small.csv"), actual.get("joined"));
    }

    private void writeSources() throws IOException {
        Files.writeString(this.src.resolve("big.csv"), "key,value\n1,a\n2,b\n");
        Files.writeString(this.src.resolve("small.csv"), "key,other\n1,x\n");
        Files.createDirectories(this.src.resolve("sub"));
        Files.writeString(this.src.resolve("sub/nested.csv"), "key\n1\n");
    }

    private ComparableDataSetParam param(final String setting) {
        return DataSetTestSupport.param(setting).toBuilder()
                .setSource(DataSourceType.csv)
                .setSrc(this.src.toFile())
                .setEncoding("UTF-8")
                .setRecursive(true)
                .build();
    }

    /**
     * テーブル名→読込元の表示用のパス
     */
    private Map<String, List<String>> tableSources(final String setting) {
        final ComparableDataSetParam param = this.param(setting);
        return new ComparableCsvDataSetProducer(param).tableSources().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey
                        , entry -> entry.getValue().stream()
                                .map(source -> param.srcPath(source).display())
                                .sorted()
                                .toList()
                        , (a, b) -> a, TreeMap::new));
    }
}
