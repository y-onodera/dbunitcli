package yo.dbunitcli.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.command.Type;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

class CommandParametersTest {

    @Test
    void resolveFilePaths_パス変更時にPオプションが保持される() {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("-srcType", "csv");
        input.put("-src", "some/path");
        input.put("-setting", "relative/setting.json");
        input.put("-resultType", "csv");
        input.put("-result", "some/result");
        input.put("-Pmykey", "myvalue");

        final CommandParameters params = new CommandParameters(Type.convert, input)
                .resolveFilePaths((baseDir, value) ->
                        baseDir == Option.BaseDir.SETTING
                                ? "/abs/" + value
                                : value);

        final boolean containsPArg = Arrays.stream(params.args())
                .anyMatch(it -> it.equals("-Pmykey=myvalue"));
        Assertions.assertTrue(containsPArg, "-Pmykey=myvalue がresolveFilePaths後も保持されること");
    }

    @Test
    void resolveFilePaths_パス変更なし時は同一インスタンスを返す() {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("-srcType", "csv");
        input.put("-src", "some/path");
        input.put("-Pmykey", "myvalue");

        final CommandParameters original = new CommandParameters(Type.convert, input);
        final CommandParameters result = original.resolveFilePaths((baseDir, value) -> value);

        Assertions.assertSame(original, result, "パス変更がない場合は同一インスタンスを返すこと");
    }

    @Test
    void resolveFilePaths_複数のPオプションがすべて保持される() {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("-srcType", "csv");
        input.put("-src", "some/path");
        input.put("-setting", "relative/setting.json");
        input.put("-resultType", "csv");
        input.put("-result", "some/result");
        input.put("-Pkey1", "value1");
        input.put("-Pkey2", "value2");

        final CommandParameters params = new CommandParameters(Type.convert, input)
                .resolveFilePaths((baseDir, value) ->
                        baseDir == Option.BaseDir.SETTING
                                ? "/abs/" + value
                                : value);

        final String[] args = params.args();
        Assertions.assertTrue(Arrays.stream(args).anyMatch(it -> it.equals("-Pkey1=value1")),
                "-Pkey1=value1 が保持されること");
        Assertions.assertTrue(Arrays.stream(args).anyMatch(it -> it.equals("-Pkey2=value2")),
                "-Pkey2=value2 が保持されること");
    }
}
