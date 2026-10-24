package yo.dbunitcli.application.option;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.ResultType;

class DataSetConverterOptionTest {

    @Test
    void parallelismIsNullWhenOmitted() {
        final DataSetConverterOption target = new DataSetConverterOption("result", new DataSetConverterDto());
        Assertions.assertNull(target.parallelism());
        Assertions.assertEquals("", target.toParameters().get("-parallel"));
    }

    @Test
    void parallelismIsParsed() {
        final DataSetConverterDto dto = new DataSetConverterDto();
        dto.setParallel(" 4 ");
        final DataSetConverterOption target = new DataSetConverterOption("result", dto);
        Assertions.assertEquals(4, target.parallelism());
        Assertions.assertEquals("4", target.toParameters().get("-parallel"));
    }

    @Test
    void parallelMustBePositiveNumber() {
        final DataSetConverterDto zero = new DataSetConverterDto();
        zero.setParallel("0");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DataSetConverterOption("result", zero));
        final DataSetConverterDto text = new DataSetConverterDto();
        text.setParallel("auto");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DataSetConverterOption("result", text));
    }

    @Test
    void parallelIsNotShownForTable() {
        final DataSetConverterDto dto = new DataSetConverterDto();
        dto.setResultType(ResultType.table);
        Assertions.assertFalse(new DataSetConverterOption("result", dto).toParameters()
                .options().containsKey("-result.parallel"));
    }
}
