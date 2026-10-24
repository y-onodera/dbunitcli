package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.dataset.producer.ComparableFixedColumnDefMetaDataProducer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * unit=tableで、ラッパーの変換（DB入力ならメタデータ照会を伴う）をテーブル一覧の作成では行わず、テーブルごとの出力で1回だけ行うこと
 */
public class WrapperConvertOnceTest {

    private static final List<String> TABLES = List.of("a", "b", "c");

    @TempDir
    Path tempDir;

    private File src;

    @BeforeEach
    public void setUp() throws IOException {
        this.src = Files.createDirectories(this.tempDir.resolve("src")).toFile();
        for (final String name : WrapperConvertOnceTest.TABLES) {
            Files.writeString(new File(this.src, name + ".csv").toPath(), "\"ID\",\"VAL\"\n\"1\",\"x\"\n", StandardCharsets.UTF_8);
        }
    }

    @Test
    public void table_unit_converts_each_table_once() {
        final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
        final CountingProducer producer = new CountingProducer(new ComparableCsvDataSetProducer(ComparableDataSetParam.builder()
                .setSrc(this.src)
                .setSource(DataSourceType.csv)
                .setEncoding("UTF-8")
                .setLoadData(false)
                .build()), counts);
        final List<ParameterUnit.TableTemplate> templates = ParameterUnit.tableTemplates(
                new ComparableDataSetLoader(Parameter.none()), producer, TableSeparators.NONE).toList();
        // テーブル一覧の作成ではテーブル名とスキーマだけを求め、変換しない
        Assertions.assertTrue(counts.isEmpty(), counts.toString());
        final List<Parameter> loaded = templates.stream().map(ParameterUnit.TableTemplate::load).toList();
        Assertions.assertEquals(WrapperConvertOnceTest.TABLES.size(), loaded.size());
        WrapperConvertOnceTest.TABLES.forEach(it -> Assertions.assertEquals(1, counts.get(it).get(), it));
        // 1回目の変換結果から行を作る（列ごとに1行）
        for (final Parameter parameter : loaded) {
            final List<?> rows = (List<?>) parameter.get("rows");
            Assertions.assertEquals(List.of("ID", "VAL"), rows.stream().map(it -> ((Map<?, ?>) it).get("name")).toList());
        }
    }

    private static class CountingProducer extends ComparableFixedColumnDefMetaDataProducer {

        private final Map<String, AtomicInteger> counts;

        CountingProducer(final ComparableDataSetProducer delegate, final Map<String, AtomicInteger> counts) {
            super(delegate, new String[0], 10, "left");
            this.counts = counts;
        }

        @Override
        protected void convertTable(final ITableMetaData metaData, final Conversion conversion) throws DataSetException {
            this.counts.computeIfAbsent(metaData.getTableName(), k -> new AtomicInteger()).incrementAndGet();
            super.convertTable(metaData, conversion);
        }
    }
}
