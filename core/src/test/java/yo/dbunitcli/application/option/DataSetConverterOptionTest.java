package yo.dbunitcli.application.option;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.ResultType;

import java.util.List;

class DataSetConverterOptionTest {

    @Test
    void parallelismIsNullWhenOmitted() {
        final DataSetConverterOption target = new DataSetConverterOption("result", new DataSetConverterDto());
        Assertions.assertNull(target.parallelism());
        Assertions.assertEquals("", target.toParametersBuilder(true).build().get("-parallel"));
    }

    @Test
    void parallelismIsParsed() {
        final DataSetConverterDto dto = new DataSetConverterDto();
        dto.setParallel(" 4 ");
        final DataSetConverterOption target = new DataSetConverterOption("result", dto);
        Assertions.assertEquals(4, target.parallelism());
        Assertions.assertEquals("4", target.toParametersBuilder(true).build().get("-parallel"));
    }

    @Test
    void parallelMustBePositiveNumber() {
        final DataSetConverterDto zero = new DataSetConverterDto();
        zero.setParallel("0");
        Assertions.assertEquals(List.of("Invalid value for option '-result.parallel': must be 1 or more but was '0'")
                , new DataSetConverterOption("result", zero).validate().stream().map(Option.Violation::message).toList());
        final DataSetConverterDto text = new DataSetConverterDto();
        text.setParallel("auto");
        Assertions.assertEquals(List.of("Invalid value for option '-result.parallel': 'auto' is not an int")
                , new DataSetConverterOption("result", text).validate().stream().map(Option.Violation::message).toList());
    }

    @Test
    void parallelIsShownOnlyWhenRequested() {
        final DataSetConverterDto dto = new DataSetConverterDto();
        dto.setResultType(ResultType.table);
        final DataSetConverterOption target = new DataSetConverterOption("result", dto);
        Assertions.assertTrue(target.toParametersBuilder(true).build().options().containsKey("-result.parallel"));
        Assertions.assertFalse(target.toParameters().options().containsKey("-result.parallel"));
    }

    @Test
    void excelIsNotParallelizableOnlyForSheet() {
        final DataSetConverterDto dto = new DataSetConverterDto();
        dto.setResultType(ResultType.xlsx);
        Assertions.assertFalse(new DataSetConverterOption("result", dto).isParallelizable());
        dto.setExcelTable("BOOK");
        Assertions.assertTrue(new DataSetConverterOption("result", dto).isParallelizable());
        // 環境変数など実行時に決まる値は、SHEETと確定していないので表示する
        dto.setExcelTable("$excelTable$");
        Assertions.assertTrue(new DataSetConverterOption("result", dto).isParallelizable());
        dto.setResultType(ResultType.csv);
        dto.setExcelTable(null);
        Assertions.assertTrue(new DataSetConverterOption("result", dto).isParallelizable());
    }
}
