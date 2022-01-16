package yo.dbunitcli.application.component;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DataSetLoadOptionTest {
    private String[] expect = new String[]{"-src=test"
            , "-srcType=csv"
            , "-setting=setting.js"
            , "-loadData=true"
            , "-encoding=UTF-8"
            , "-regInclude=*.\\.csv"
            , "-regExclude=*.\\.csv"};

    private List<String> expectList = Lists.newArrayList(expect);

    @Test
    public void testFilterArguments() {
        DataSetLoadOption target = new DataSetLoadOption("");
        CmdLineParser parser = new CmdLineParser(target);
        expectList.add("-other=other");
        Collection<String> result = target.filterArguments(parser, expectList.toArray(new String[expectList.size()]));
        assertArrayEquals(Arrays.stream(expect).sorted().toArray(), result.stream().sorted().toArray());
    }

    @Test
    public void testFilterArgumentsOverride() {
        DataSetLoadOption target = new DataSetLoadOption("new");
        CmdLineParser parser = new CmdLineParser(target);
        List<String> argsList = Lists.newArrayList("-new.src=replace", "-other=other");
        argsList.addAll(expectList);
        Collection<String> result = target.filterArguments(parser, argsList.toArray(new String[argsList.size()]));
        assertArrayEquals(Arrays.stream(expect).map(it -> it.equals("-src=test") ? "-src=replace" : it).sorted().toArray()
                , result.stream().sorted().toArray());
    }
}