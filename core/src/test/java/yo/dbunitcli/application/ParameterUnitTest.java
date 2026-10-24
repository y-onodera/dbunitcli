package yo.dbunitcli.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ParameterUnitTest {

    @TempDir
    Path tempDir;

    private File src;

    @BeforeEach
    public void setUp() throws IOException {
        this.src = Files.createDirectories(this.tempDir.resolve("src")).toFile();
        Files.writeString(new File(this.src, "a.csv").toPath(), "ID\na0\na1\n", StandardCharsets.UTF_8);
        Files.writeString(new File(this.src, "b.csv").toPath(), "ID\nb0\nb1\nb2\n", StandardCharsets.UTF_8);
    }

    @Test
    public void record_numbers_rows_per_table_with_table_name() {
        final List<String> actual = this.load(false).stream()
                .map(it -> it.get("tableName") + ":" + it.get("rowNumber") + ":" + it.get("ID"))
                .toList();
        Assertions.assertEquals(List.of("a:0:a0", "a:1:a1", "b:0:b0", "b:1:b1", "b:2:b2"), actual);
    }

    @Test
    public void record_with_metadata_numbers_rows_per_table() {
        final List<String> actual = this.load(true).stream()
                .map(it -> it.get("tableName") + ":" + it.get("rowNumber") + ":" + ((Map<?, ?>) it.get("row")).get("ID")
                        + ":" + ((List<?>) it.get("rows")).size())
                .toList();
        Assertions.assertEquals(List.of("a:0:a0:2", "a:1:a1:2", "b:0:b0:3", "b:1:b1:3", "b:2:b2:3"), actual);
    }

    @Test
    public void record_without_source_is_one_parameter() {
        final List<Parameter> actual = ParameterUnit.record.dataSetToStream(new ComparableDataSetLoader(Parameter.none())
                , ComparableDataSetParam.builder().setSource(DataSourceType.none).build()).toList();
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(0, actual.getFirst().get("rowNumber"));
    }

    private List<Parameter> load(final boolean includeMetaData) {
        return ParameterUnit.record.dataSetToStream(new ComparableDataSetLoader(Parameter.none())
                , ComparableDataSetParam.builder()
                        .setSrc(this.src)
                        .setSource(DataSourceType.csv)
                        .setEncoding("UTF-8")
                        .setMapIncludeMetaData(includeMetaData)
                        .build()).toList();
    }
}
