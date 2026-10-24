package yo.dbunitcli.application.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.Option;

/**
 * -result.parallel（generateは-parallel）は並列化できる組み合わせのときだけ表示する
 */
public class ParallelParameterTest {

    @Test
    public void convertShowsParallelWhenSourceAndResultAreParallelizable() {
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=fixed", "-resultType=fixed"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=reg", "-resultType=format"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=table"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=xlsx", "-excelTable=BOOK"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=xlsx", "-excelTable=${EXCEL_TABLE}"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=xlsx", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=xls", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csvq", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=table", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=sql", "-resultType=table"));
    }

    @Test
    public void convertHidesParallelWhenSourceIsNotParallelizable() {
        Assertions.assertFalse(this.convertHasParallel("-srcType=file", "-resultType=csv"));
        Assertions.assertFalse(this.convertHasParallel("-srcType=dir", "-resultType=csv"));
    }

    @Test
    public void convertHidesParallelWhenResultIsOneBook() {
        Assertions.assertFalse(this.convertHasParallel("-srcType=csv", "-resultType=xlsx"));
        Assertions.assertFalse(this.convertHasParallel("-srcType=csv", "-resultType=xls", "-excelTable=SHEET"));
    }

    @Test
    public void compareShowsParallelWhenNewOrOldIsParallelizable() {
        Assertions.assertTrue(this.compareHasParallel("-new.srcType=csv", "-old.srcType=csv"));
        Assertions.assertTrue(this.compareHasParallel("-new.srcType=xlsx", "-old.srcType=csv"));
        // 比較では出力形式（1ブックのxlsx）は読込の並列化に関係しない
        Assertions.assertTrue(this.compareHasParallel("-new.srcType=csv", "-old.srcType=csv", "-resultType=xlsx"));
    }

    @Test
    public void compareHidesParallelWhenNeitherIsParallelizable() {
        Assertions.assertFalse(this.compareHasParallel("-new.srcType=file", "-old.srcType=dir"));
        Assertions.assertFalse(this.compareHasParallel("-targetType=image", "-new.srcType=csv", "-old.srcType=csv"));
    }

    @Test
    public void generateShowsParallelWhenSourceAndGenerateTypeAreParallelizable() {
        Assertions.assertTrue(this.generateHasParallel("-srcType=csv", "-generateType=txt", "-unit=table"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=csv", "-generateType=txt"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=xlsx", "-generateType=xlsx", "-unit=table"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=table", "-generateType=sql"));
        // srcのproducerを包むgenerateTypeも、包んだproducerが並列読込できれば並列化する
        Assertions.assertTrue(this.generateHasParallel("-srcType=table", "-generateType=ddl"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=table", "-generateType=javaBean"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=csv", "-generateType=xlsxSchema"));
        Assertions.assertTrue(this.generateHasParallel("-srcType=csv", "-generateType=fixedColumnDef"));
    }

    @Test
    public void generateHidesParallelWhenSourceIsNotParallelizable() {
        Assertions.assertFalse(this.generateHasParallel("-srcType=file", "-generateType=txt", "-unit=table"));
        Assertions.assertFalse(this.generateHasParallel("-srcType=dir", "-generateType=fixedColumnDef"));
    }

    @Test
    public void generateParallelIsPassedToSourceLoading() {
        Assertions.assertEquals(2, new Generate().parseOption(new String[]{"-srcType=csv", "-parallel=2"}).dataSetParam().parallel());
        Assertions.assertNull(new Generate().parseOption(new String[]{"-srcType=csv"}).dataSetParam().parallel());
    }

    @Test
    public void generateRejectsInvalidParallel() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Generate().parseOption(new String[]{"-srcType=csv", "-parallel=0"}));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Generate().parseOption(new String[]{"-srcType=csv", "-parallel=a"}));
    }

    private boolean generateHasParallel(final String... args) {
        return new Generate().parseOption(args).toParameters().options().containsKey("-parallel");
    }

    private boolean convertHasParallel(final String... args) {
        return this.hasParallel(new Convert().parseOption(args).toParameters());
    }

    private boolean compareHasParallel(final String... args) {
        return this.hasParallel(new Compare().parseOption(args).toParameters());
    }

    private boolean hasParallel(final Option.Parameters parameters) {
        return parameters.subComponents().get("convertResult").options().containsKey("-result.parallel");
    }
}
