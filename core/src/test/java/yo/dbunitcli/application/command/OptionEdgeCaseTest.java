package yo.dbunitcli.application.command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.dto.ImageCompareDto;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionEdgeCaseTest {

    private static final String PARAMETER_DIR = "src/test/resources/yo/dbunitcli/application/command";

    private final Properties backup = new Properties();

    @TempDir
    Path temp;

    @BeforeEach
    void setUp() {
        this.backup.putAll(System.getProperties());
    }

    @AfterEach
    void tearDown() {
        System.setProperties(this.backup);
    }

    @Test
    void expectedDiffIsWrittenUnderResultBase() {
        System.setProperty(FileResources.PROPERTY_RESULT_BASE, this.temp.toString());
        final CompareOption option = new Compare().parseOption(new String[]{
                "@" + PARAMETER_DIR + "/paramCompareWithRow.txt"
                , "-result=out"
        });
        option.compare();
        assertTrue(new File(this.temp.toFile(), "out/expectedDiff").isDirectory());
    }

    @Test
    void parameterFileWithoutExtensionUsesWholeNameAsResultFile() throws IOException {
        final Path param = this.temp.resolve("noext");
        Files.writeString(param, "-src.srcType=csv\n-src.src=.\n", StandardCharsets.UTF_8);
        final ConvertOption option = new Convert().parseOption(new String[]{"@" + param});
        assertEquals("noext", option.result().resultFile());
    }

    @Test
    void emptyImageCompareValuesUseDefaults() {
        final ImageCompareDto dto = new ImageCompareDto();
        dto.setThreshold("");
        dto.setPixelToleranceLevel("");
        dto.setAllowingPercentOfDifferentPixels("");
        dto.setRectangleLineWidth("");
        dto.setMinimalRectangleSize("");
        dto.setMaximalRectangleCount("");
        dto.setPercentOpacityDifferenceRectangles("");
        dto.setPercentOpacityExcludedRectangles("");
        final ImageCompareOption option = new ImageCompareOption("image", dto);
        assertTrue(option.validate().isEmpty());
        assertDoesNotThrow(() -> option.createFactoryOf(CompareOption.Type.image));
    }

    @Test
    void parameterizeArgsToleratesBlankLinesAndDuplicatesWithOverride() throws IOException {
        final Path template = this.temp.resolve("template.txt");
        Files.writeString(template, "-src.srcType=csv\n\n-result=first\n\n-result=second\n", StandardCharsets.UTF_8);
        final ParameterizeOption option = new Parameterize().parseOption(new String[]{
                "-cmd=convert"
                , "-template=" + template
                , "-arg=-src.src=override"
        });
        final List<String> args = Arrays.asList(option.createArgs(Parameter.none()));
        assertEquals(List.of("-src.srcType=csv", "-result=second", "-src.src=override"), args);
    }

    @Test
    void settingFilesSeparatedByCommaAndSpaceAreLoaded() {
        final String setting = new File("src/test/resources/yo/dbunitcli/application/settings/filter/setting.json")
                .getAbsolutePath();
        final CompareOption option = new Compare().parseOption(new String[]{"-setting=" + setting + ", " + setting});
        assertTrue(option.validate().stream().noneMatch(it -> it.options().contains("-setting")));
        assertDoesNotThrow(option::getTableSeparators);
    }

    @Test
    void laterArgumentOverridesEarlierSameOption() {
        final ConvertOption option = new Convert().parseOption(new String[]{
                "-result.resultPath=first"
                , "-result.resultPath=second"
        });
        assertEquals("second", option.result().getResultPath());
    }
}
