package yo.dbunitcli.application.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.Option;

/**
 * -result.parallel は並列化できる組み合わせのときだけ表示する
 */
public class ParallelParameterTest {

    @Test
    public void convertShowsParallelWhenSourceAndResultAreParallelizable() {
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=csv"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=fixed", "-resultType=fixed"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=reg", "-resultType=format"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=table"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=csv", "-resultType=xlsx", "-excelTable=BOOK"));
        Assertions.assertTrue(this.convertHasParallel("-srcType=xlsx", "-resultType=csv"));
    }

    @Test
    public void convertHidesParallelWhenSourceIsNotParallelizable() {
        Assertions.assertFalse(this.convertHasParallel("-srcType=xls", "-resultType=csv"));
        Assertions.assertFalse(this.convertHasParallel("-srcType=csvq", "-resultType=csv"));
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
        Assertions.assertFalse(this.compareHasParallel("-new.srcType=xls", "-old.srcType=csvq"));
        Assertions.assertFalse(this.compareHasParallel("-targetType=image", "-new.srcType=csv", "-old.srcType=csv"));
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
