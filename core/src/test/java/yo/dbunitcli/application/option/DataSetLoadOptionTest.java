package yo.dbunitcli.application.option;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void toCommandLineArgs() {
        final Option.CommandLineArgs result = this.target.toCommandLineArgs();
        final Option.Arg srcType = result.getArg("-srcType");
        Assertions.assertEquals(Arrays.stream(DataSourceType.values())
                .filter(it -> it != DataSourceType.none)
                .map(Enum::toString)
                .toList(), srcType.attribute().selectOption());
    }
}