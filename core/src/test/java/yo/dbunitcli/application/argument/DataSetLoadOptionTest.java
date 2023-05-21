package yo.dbunitcli.application.argument;

import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;
import java.util.Collection;
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
    ;

    @Test
    public void testFilterArguments() {
        final DataSetLoadOption target = new DataSetLoadOption("");
        final CmdLineParser parser = new CmdLineParser(target);
        this.expectList.add("-other=other");
        final Collection<String> result = target.filterArguments(parser, this.expectList.toArray(new String[this.expectList.size()]));
        assertArrayEquals(Arrays.stream(this.expect).sorted().toArray(), result.stream().sorted().toArray());
    }

    @Test
    public void testFilterArgumentsOverride() {
        final DataSetLoadOption target = new DataSetLoadOption("new");
        final CmdLineParser parser = new CmdLineParser(target);
        final List<String> argsList = Arrays.stream(new String[]{"-new.src=replace", "-other=other"}).collect(Collectors.toList());
        argsList.addAll(this.expectList);
        final Collection<String> result = target.filterArguments(parser, argsList.toArray(new String[argsList.size()]));
        assertArrayEquals(Arrays.stream(this.expect).map(it -> it.equals("-src=test") ? "-src=replace" : it).sorted().toArray()
                , result.stream().sorted().toArray());
    }
}