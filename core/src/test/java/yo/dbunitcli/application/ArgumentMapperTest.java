package yo.dbunitcli.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentMapperTest {

    @TempDir
    Path workspace;

    @BeforeEach
    void setUp() {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, this.workspace.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
        System.clearProperty(FileResources.PROPERTY_ENV);
    }

    @Test
    void replaceEnvFromDefaultEnvProperties() throws IOException {
        Files.writeString(this.workspace.resolve("env.properties")
                , "host=localhost\nport=5432\nuser=ユーザー\n", StandardCharsets.UTF_8);
        final TestDto dto = new TestDto();
        new ArgumentMapper("").populate(new String[]{
                "-url=jdbc:postgresql://${env.host}:${env.port}/db"
                , "-user=${env.user}"
        }, dto);
        assertEquals("jdbc:postgresql://localhost:5432/db", dto.url);
        assertEquals("ユーザー", dto.user);
    }

    @Test
    void replaceEnvFromSpecifiedEnvProperties() throws IOException {
        Files.createDirectories(this.workspace.resolve("env"));
        Files.writeString(this.workspace.resolve("env/staging.properties"), "host=staging\n", StandardCharsets.UTF_8);
        System.setProperty(FileResources.PROPERTY_ENV, "env/staging.properties");
        final TestDto dto = new TestDto();
        new ArgumentMapper("").populate(new String[]{"-url=jdbc:h2:tcp://${env.host}/db"}, dto);
        assertEquals("jdbc:h2:tcp://staging/db", dto.url);
    }

    @Test
    void notReplaceWithoutEnvVariable() {
        final TestDto dto = new TestDto();
        new ArgumentMapper("").populate(new String[]{"-url=${host}", "-user=$user$"}, dto);
        assertEquals("${host}", dto.url);
        assertEquals("$user$", dto.user);
    }

    @Test
    void throwIfEnvVariableNotDefined() {
        final TestDto dto = new TestDto();
        assertThrows(AssertionError.class
                , () -> new ArgumentMapper("").populate(new String[]{"-url=${env.host}"}, dto));
    }

    static class TestDto {
        @CommandLine.Option(names = "-url")
        String url;
        @CommandLine.Option(names = "-user")
        String user;
    }
}
