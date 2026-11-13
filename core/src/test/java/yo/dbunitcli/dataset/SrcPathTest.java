package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.common.Source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

class SrcPathTest {

    @TempDir
    Path src;

    @Test
    void srcの中のファイルは相対パスで表示しsrcを入力のまま辿ったパスで照合する() throws IOException {
        final Path nested = this.write("sub/nested.csv");

        final ComparableDataSetParam.SrcPath actual = this.param(null).srcPath(new Source(nested.toFile(), false));

        Assertions.assertEquals("sub/nested.csv", actual.display());
        Assertions.assertEquals(this.src.resolve("sub").resolve("nested.csv").toString(), actual.filterTarget());
    }

    @Test
    void 照合用のパスを完全一致の正規表現にするとそのファイルだけを辿る() throws IOException {
        final Path nested = this.write("sub/nested.csv");
        this.write("other.csv");
        final String target = this.param(null).srcPath(new Source(nested.toFile(), false)).filterTarget();

        final List<File> actual = this.param("^" + Pattern.quote(target) + "$").getSrcFiles().toList();

        Assertions.assertEquals(List.of(nested.toFile()), actual);
    }

    @Test
    void srcの外のファイルはそのパスを返す() {
        final Source outside = new Source(this.src.getParent().resolve("outside.csv").toFile(), false);

        final ComparableDataSetParam.SrcPath actual = this.param(null).srcPath(outside);

        Assertions.assertEquals(new ComparableDataSetParam.SrcPath(outside.filePath(), outside.filePath()), actual);
    }

    private Path write(final String relative) throws IOException {
        final Path file = this.src.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "key\n1\n");
        return file;
    }

    private ComparableDataSetParam param(final String regInclude) {
        return ComparableDataSetParam.builder()
                .setSrc(this.src.toFile())
                .setRecursive(true)
                .setRegInclude(regInclude)
                .build();
    }
}
