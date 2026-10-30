package yo.dbunitcli.application.option;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static yo.dbunitcli.dataset.TableSeparatorsTest.outputNames;

class DataSetLoadOptionTest {

    private final DataSetLoadDto src = new DataSetLoadDto();
    private DataSetLoadOption target;

    @BeforeEach
    void setUp() {
        this.target = new DataSetLoadOption("test", this.src);
    }

    @Test
    void toParameters() {
        final Option.Parameters result = this.target.toParameters();
        final Option.Arg srcType = result.getArg("-srcType");
        Assertions.assertEquals(Arrays.stream(DataSourceType.values())
                .filter(it -> it != DataSourceType.none)
                .map(Enum::toString)
                .toList(), srcType.attribute().selectOption());
    }

    @ParameterizedTest
    @CsvSource({"sql,SQL", "table,TABLE", "csvq,CSVQ", "csv,DATASET"})
    void srcの基準ディレクトリはsrcTypeで決まる(final DataSourceType srcType, final Option.BaseDir expected) {
        this.src.setSrcType(srcType);
        final DataSetLoadOption option = new DataSetLoadOption("test", this.src);
        Assertions.assertEquals(expected, option.toParameters().getArg("-src").attribute().defaultPath());
    }

    @TempDir
    Path tempDir;

    @Test
    void settingTargetOnlyを指定しなければ設定の対象外のテーブルも読み込む() throws IOException {
        this.src.setSetting(this.writeSetting("{\"settings\":[{\"name\":[\"big\"],\"tableName\":\"renamed\"}]}"));

        final TableSeparators separators = new DataSetLoadOption("test", this.src).getTableSeparators();

        Assertions.assertEquals(List.of("renamed"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of("small"), outputNames(separators, "small"));
    }

    @Test
    void settingTargetOnlyなら設定の対象のテーブルだけを読み込む() throws IOException {
        this.src.setSetting(this.writeSetting("{\"settings\":[{\"name\":[\"big\"],\"tableName\":\"renamed\"}],\"commonSettings\":[{}]}"));
        this.src.setSettingTargetOnly("true");

        final TableSeparators separators = new DataSetLoadOption("test", this.src).getTableSeparators();

        Assertions.assertEquals(List.of("renamed"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of(), outputNames(separators, "small"));
    }

    @Test
    void settingTargetOnlyでもsettingが無ければ絞らない() {
        this.src.setSettingTargetOnly("true");

        final TableSeparators separators = new DataSetLoadOption("test", this.src).getTableSeparators();

        Assertions.assertEquals(List.of("small"), outputNames(separators, "small"));
    }

    @Test
    void settingTargetOnlyでsettingが無ければsettingTargetOnlyとsettingの両方を違反にする() {
        this.src.setSettingTargetOnly("true");

        Assertions.assertEquals(List.of(List.of("-test.settingTargetOnly", "-test.setting")),
                this.settingTargetOnlyViolations());
    }

    @Test
    void settingTargetOnlyでsettingがあれば違反にしない() throws IOException {
        this.src.setSetting(this.writeSetting("{\"settings\":[{\"name\":[\"big\"]}]}"));
        this.src.setSettingTargetOnly("true");

        Assertions.assertEquals(List.of(), this.settingTargetOnlyViolations());
    }

    @Test
    void settingTargetOnlyで全テーブルに適用するcommonSettingsがあればすべて読み込む() throws IOException {
        this.src.setSetting(this.writeSetting("{\"settings\":[{\"name\":[\"big\"],\"tableName\":\"renamed\"}],\"commonSettings\":[{\"keys\":[\"key\"]}]}"));
        this.src.setSettingTargetOnly("true");

        final TableSeparators separators = new DataSetLoadOption("test", this.src).getTableSeparators();

        Assertions.assertEquals(List.of("renamed"), outputNames(separators, "big"));
        Assertions.assertEquals(List.of("small"), outputNames(separators, "small"));
    }

    @Test
    void settingTargetOnlyはtrueかfalseのみ() {
        this.src.setSettingTargetOnly("yes");

        Assertions.assertTrue(new DataSetLoadOption("test", this.src).validate().stream()
                                                                    .anyMatch(it -> it.options().contains("-test.settingTargetOnly")));
    }

    private List<List<String>> settingTargetOnlyViolations() {
        return new DataSetLoadOption("test", this.src).validate().stream()
                                                      .map(Option.Violation::options)
                                                      .filter(it -> it.contains("-test.settingTargetOnly"))
                                                      .toList();
    }

    private String writeSetting(final String content) throws IOException {
        final Path file = this.tempDir.resolve("setting.json");
        Files.writeString(file, content);
        return file.toAbsolutePath().toString();
    }
}
