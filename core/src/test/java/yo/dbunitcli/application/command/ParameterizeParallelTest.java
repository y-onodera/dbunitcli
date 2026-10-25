package yo.dbunitcli.application.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * -parallelを2以上にすると、パラメータごとのコマンドを並列に実行する
 */
public class ParameterizeParallelTest {

    private static final List<String> NAMES = IntStream.rangeClosed(1, 6).mapToObj(it -> "t" + it).toList();

    @TempDir
    Path tempDir;

    private File paramCsv;

    private File setting;

    @BeforeEach
    public void setUp() throws IOException {
        for (final String name : ParameterizeParallelTest.NAMES) {
            this.write(new File(this.dir("data/" + name), name + ".csv"), "ID,VAL\n1," + name + "\n");
            // t1・t2だけcompareで差分が出る
            this.write(new File(this.dir("expect/" + name), name + ".csv")
                    , "ID,VAL\n1," + (name.equals("t1") || name.equals("t2") ? "diff" : name) + "\n");
        }
        this.setting = new File(this.tempDir.toFile(), "setting.json");
        this.write(this.setting, "{\"commonSettings\":[{\"keys\":[\"ID\"]}]}");
        this.paramCsv = new File(this.dir("param"), "param.csv");
        this.write(this.paramCsv, "name\n" + String.join("\n", ParameterizeParallelTest.NAMES) + "\n");
    }

    @Test
    public void parallelDefaultIsSequential() {
        final ParameterizeOption target = new Parameterize().parseOption(new String[]{"-cmd=convert"});
        Assertions.assertEquals(1, target.parallelism());
        Assertions.assertEquals("", target.toParameters().getArg("-parallel").value());
    }

    @Test
    public void parallelMustBeOneOrMore() {
        Assertions.assertEquals(List.of("Invalid value for option '-parallel': must be 1 or more but was '0'")
                , new Parameterize().parseOption(new String[]{"-cmd=convert", "-parallel=0"}).validate().stream()
                                    .map(Option.Violation::message).toList());
    }

    @Test
    public void parallelRunsCommandForEveryParameter() throws IOException {
        final File template = this.template("convert.txt"
                , "-src=" + this.path("data") + "/$name$"
                , "-srcType=csv"
                , "-resultType=csv"
                , "-result=" + this.path("result") + "/$name$");
        this.exec("-cmd=convert", "-template=" + template.getAbsolutePath(), "-parallel=3");
        for (final String name : ParameterizeParallelTest.NAMES) {
            // 改行コードはOSに依存するため行単位で比べる
            Assertions.assertEquals(List.of("\"ID\",\"VAL\"", "\"1\",\"" + name + "\"")
                    , Files.readAllLines(this.tempDir.resolve("result/" + name + "/" + name + ".csv"), StandardCharsets.UTF_8), name);
        }
    }

    @Test
    public void parallelWithIgnoreFailRunsAllAndFailsAtLast() throws IOException {
        final File template = this.compareTemplate();
        final Command.CommandFailException actual = Assertions.assertThrows(Command.CommandFailException.class
                , () -> this.exec("-cmd=compare", "-template=" + template.getAbsolutePath(), "-parallel=3", "-ignoreFail=true"));
        Assertions.assertEquals("execute has fail count", actual.getMessage());
        for (final String name : ParameterizeParallelTest.NAMES) {
            Assertions.assertTrue(this.tempDir.resolve("compare/" + name).toFile().exists(), name);
        }
    }

    @Test
    public void parallelWithoutIgnoreFailRethrowsFailureOfCommand() throws IOException {
        final File template = this.compareTemplate();
        final Command.CommandFailException actual = Assertions.assertThrows(Command.CommandFailException.class
                , () -> this.exec("-cmd=compare", "-template=" + template.getAbsolutePath(), "-parallel=3"));
        Assertions.assertEquals("unexpected diff found.", actual.getMessage());
    }

    private File compareTemplate() throws IOException {
        return this.template("compare.txt"
                , "-old.src=" + this.path("data") + "/$name$"
                , "-old.srcType=csv"
                , "-new.src=" + this.path("expect") + "/$name$"
                , "-new.srcType=csv"
                , "-setting=" + this.setting.getAbsolutePath().replace('\\', '/')
                , "-result=" + this.path("compare") + "/$name$");
    }

    private void exec(final String... args) {
        final String[] common = {
                "-param.src=" + this.paramCsv.getAbsolutePath()
                , "-param.srcType=csv"
                , "-templateParameterAttribute="};
        final String[] all = Stream.concat(Arrays.stream(common), Arrays.stream(args))
                .toArray(String[]::new);
        final Parameterize target = new Parameterize();
        FileResources.runInContext(() -> target.exec(target.parseOption(all)));
    }

    private File template(final String name, final String... lines) throws IOException {
        final File result = new File(this.dir("template"), name);
        this.write(result, String.join("\n", lines));
        return result;
    }

    private String path(final String name) {
        return this.tempDir.resolve(name).toFile().getAbsolutePath().replace('\\', '/');
    }

    private File dir(final String name) {
        final File result = this.tempDir.resolve(name).toFile();
        result.mkdirs();
        return result;
    }

    private void write(final File file, final String content) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }
}
