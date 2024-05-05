package yo.dbunitcli.application.cli;

import picocli.CommandLine;

import java.util.function.Function;

public interface ArgumentFilter {

    String[] filterArguments(String prefix, CommandLine commandLine, String[] expandArgs);

    default Function<String, String> extractKey() {
        return it -> it.replaceAll("(-[^=]+=).+", "$1");
    }


}
