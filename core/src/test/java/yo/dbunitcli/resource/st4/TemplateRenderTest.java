package yo.dbunitcli.resource.st4;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateRenderTest {

    @Test
    public void replaceParameter_replaces_attribute_with_value() {
        final Parameter parameter = Parameter.none().add("tableName", "T1");
        Assertions.assertEquals("out/T1.txt", TemplateRender.builder().setTemplateParameterAttribute("").build().replaceParameter("out/$tableName$.txt", parameter));
    }

    @Test
    public void replaceParameter_does_not_stringify_unused_value() {
        // 行の一覧など大きな値は、変数がtargetに無ければ文字列化しない
        final Object unused = new Object() {
            @Override
            public String toString() {
                throw new AssertionError("toString called");
            }
        };
        final Parameter parameter = Parameter.none().add("tableName", "T1").add("rows", unused);
        Assertions.assertEquals("out/T1.txt", TemplateRender.builder().setTemplateParameterAttribute("").build().replaceParameter("out/$tableName$.txt", parameter));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "MS932"})
    public void createSTGroup_reads_templateGroup_with_encoding(final String encoding, @TempDir final Path tempDir) throws Exception {
        final File stg = tempDir.resolve("group.stg").toFile();
        Files.writeString(stg.toPath(), "greet() ::= <<こんにちは>>\n", Charset.forName(encoding));
        final TemplateRender render = TemplateRender.builder().setEncoding(encoding).build();
        Assertions.assertEquals("こんにちは", render.createSTGroup(stg).getInstanceOf("greet").render());
    }
}
