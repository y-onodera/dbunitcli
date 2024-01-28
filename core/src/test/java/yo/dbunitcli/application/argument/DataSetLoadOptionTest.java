package yo.dbunitcli.application.argument;

import org.junit.Test;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;

public class DataSetLoadOptionTest {
    private final String[] expect = new String[]{"-src=test"
            , "-srcType=csv"
            , "-setting=setting.js"
            , "-loadData=true"
            , "-regInclude=*.\\.csv"
            , "-regExclude=*.\\.csv"};

    private final List<String> expectList = Arrays.stream(this.expect).collect(Collectors.toList());

    @Test
    public void testFilterArguments() {
        final DataSetLoadOption target = new DataSetLoadOption("");
        final CommandLine parser = new CommandLine(target);
        this.expectList.add("-other=other");
        final String[] result = target.filterArguments(parser, this.expectList.toArray(new String[0]));
        assertArrayEquals(Arrays.stream(this.expect).sorted().toArray(), Arrays.stream(result).sorted().toArray());
    }

    @Test
    public void testFilterArgumentsOverride() {
        final DataSetLoadOption target = new DataSetLoadOption("new");
        final CommandLine parser = new CommandLine(target);
        final List<String> argsList = Arrays.stream(new String[]{"-new.src=replace", "-other=other"}).collect(Collectors.toList());
        argsList.addAll(this.expectList);
        final String[] result = target.filterArguments(parser, argsList.toArray(new String[0]));
        assertArrayEquals(Arrays.stream(this.expect).map(it -> it.equals("-src=test") ? "-src=replace" : it).sorted().toArray()
                , Arrays.stream(result).sorted().toArray());
    }
}