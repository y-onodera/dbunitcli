package yo.dbunitcli.application.option;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.DataSourceType;

import java.util.Arrays;

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
}
