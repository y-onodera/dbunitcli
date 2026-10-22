package yo.dbunitcli.sidecar.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractCommandControllerTest {

    @TempDir
    Path workspace;

    @BeforeEach
    void setUp() throws IOException {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, this.workspace.toString());
        Files.createDirectories(this.workspace.resolve(FileResources.RESOURCES_SETTING_PATH));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
    }

    // オプション値はOSによらず/区切りになるため、Fileにして比較する
    private File resolveSetting(final String setting) {
        return new File(AbstractCommandController.resolveSidecarFilePaths(
                        new CommandParameters(Type.convert, new String[]{"-src.src=.", "-src.setting=" + setting}))
                .toOptionParameters()
                .getArg("-src.setting")
                .value());
    }

    @Test
    void resolveSidecarFilePaths_基準ディレクトリ配下を優先する() throws IOException {
        final Path expected = Files.createFile(this.workspace.resolve(FileResources.RESOURCES_SETTING_PATH).resolve("setting.json"));
        Files.createFile(this.workspace.resolve("setting.json"));
        assertEquals(expected.toFile().getAbsoluteFile(), this.resolveSetting("setting.json"));
    }

    @Test
    void resolveSidecarFilePaths_基準ディレクトリになければworkspaceから探す() throws IOException {
        Files.createDirectories(this.workspace.resolve("other"));
        final Path expected = Files.createFile(this.workspace.resolve("other/setting.json"));
        assertEquals(expected.toFile().getAbsoluteFile(), this.resolveSetting("other/setting.json"));
    }

    @Test
    void resolveSidecarFilePaths_どこにもなければ基準ディレクトリ配下とする() {
        assertEquals(this.workspace.resolve(FileResources.RESOURCES_SETTING_PATH).resolve("none.json").toFile().getAbsoluteFile()
                , this.resolveSetting("none.json"));
    }
}
