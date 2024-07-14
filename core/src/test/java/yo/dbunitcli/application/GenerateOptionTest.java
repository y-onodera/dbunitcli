package yo.dbunitcli.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DBConverter;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GenerateOptionTest {

    GenerateDto src = new GenerateDto();

    @BeforeEach
    void setUp() {
        this.src.setUnit(ParameterUnit.record);
        this.src.setTemplate("template.st");
        this.src.setOutputEncoding("MS932");
        this.src.setOperation(DBConverter.Operation.INSERT);
        this.src.setSqlFilePrefix("prefix");
        this.src.setSqlFileSuffix("suffix");
        this.src.setCommit("false");
        this.src.getSrcData().setSrcType(DataSourceType.table);
        this.src.getSrcData().setUseJdbcMetaData("false");
        this.src.getSrcData().setLoadData("true");
        this.src.getTemplateOption().setEncoding("UTF-8");
    }

    private GenerateOption createTarget() {
        return new GenerateOption("", GenerateOptionTest.this.src, Parameter.NONE);
    }

    @Nested
    class GenerateTypeTxt {

        @BeforeEach
        void setUp() {
            GenerateOptionTest.this.src.setGenerateType(GenerateOption.GenerateType.txt);
        }

        @Test
        void toCommandLineArgs() {
            final GenerateOption target = GenerateOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(GenerateOptionTest.this.src.getUnit().toString(), result.getArg("-unit").value());
            assertNull(result.getArg("-sqlFilePrefix"));
            assertNull(result.getArg("-sqlFileSuffix"));
            assertNull(result.getArg("-commit"));
            assertEquals(GenerateOptionTest.this.src.getOutputEncoding(), result.getArg("-outputEncoding").value());
            assertEquals(GenerateOptionTest.this.src.getTemplate(), result.getArg("-template").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getSrcType().toString(), result.getArg("-src.srcType").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getUseJdbcMetaData(), result.getArg("-src.useJdbcMetaData").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getLoadData(), result.getArg("-src.loadData").value());
            assertEquals(GenerateOptionTest.this.src.getTemplateOption().getEncoding(), result.getArg("-template.encoding").value());
        }
    }

    @Nested
    class GenerateTypeSql {

        @BeforeEach
        void setUp() {
            GenerateOptionTest.this.src.setGenerateType(GenerateOption.GenerateType.sql);
        }

        @Test
        void toCommandLineArgs() {
            final GenerateOption target = GenerateOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertNull(result.getArg("-unit"));
            assertEquals(GenerateOptionTest.this.src.getSqlFilePrefix(), result.getArg("-sqlFilePrefix").value());
            assertEquals(GenerateOptionTest.this.src.getSqlFileSuffix(), result.getArg("-sqlFileSuffix").value());
            assertEquals(GenerateOptionTest.this.src.getCommit(), result.getArg("-commit").value());
            assertEquals(GenerateOptionTest.this.src.getOutputEncoding(), result.getArg("-outputEncoding").value());
            assertNull(result.getArg("-template"));
            assertEquals(GenerateOptionTest.this.src.getSrcData().getSrcType().toString(), result.getArg("-src.srcType").value());
            assertNull(result.getArg("-src.useJdbcMetaData"));
            assertEquals(GenerateOptionTest.this.src.getSrcData().getLoadData(), result.getArg("-src.loadData").value());
            assertTrue(result.keySet().stream().filter(it -> it.startsWith("-template.")).findAny().isEmpty());
        }
    }

    @Nested
    class GenerateTypeSettings {

        @BeforeEach
        void setUp() {
            GenerateOptionTest.this.src.setGenerateType(GenerateOption.GenerateType.settings);
        }

        @Test
        void toCommandLineArgs() {
            final GenerateOption target = GenerateOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertNull(result.getArg("-unit"));
            assertNull(result.getArg("-sqlFilePrefix"));
            assertNull(result.getArg("-sqlFileSuffix"));
            assertNull(result.getArg("-commit"));
            assertEquals(GenerateOptionTest.this.src.getOutputEncoding(), result.getArg("-outputEncoding").value());
            assertNull(result.getArg("-template"));
            assertEquals(GenerateOptionTest.this.src.getSrcData().getSrcType().toString(), result.getArg("-src.srcType").value());
            assertNull(result.getArg("-src.useJdbcMetaData"));
            assertNull(result.getArg("-src.loadData"));
            assertTrue(result.keySet().stream().filter(it -> it.startsWith("-template.")).findAny().isEmpty());
        }
    }

    @Nested
    class GenerateTypeXlsx {

        @BeforeEach
        void setUp() {
            GenerateOptionTest.this.src.setGenerateType(GenerateOption.GenerateType.xlsx);
        }

        @Test
        void toCommandLineArgs() {
            final GenerateOption target = GenerateOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            assertEquals(GenerateOptionTest.this.src.getUnit().toString(), result.getArg("-unit").value());
            assertNull(result.getArg("-sqlFilePrefix"));
            assertNull(result.getArg("-sqlFileSuffix"));
            assertNull(result.getArg("-commit"));
            assertNull(result.getArg("-outputEncoding"));
            assertEquals(GenerateOptionTest.this.src.getTemplate(), result.getArg("-template").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getSrcType().toString(), result.getArg("-src.srcType").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getUseJdbcMetaData(), result.getArg("-src.useJdbcMetaData").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getLoadData(), result.getArg("-src.loadData").value());
            assertEquals(GenerateOptionTest.this.src.getTemplateOption().getEncoding(), result.getArg("-template.encoding").value());
        }
    }

    @Nested
    class GenerateTypeXls {

        @BeforeEach
        void setUp() {
            GenerateOptionTest.this.src.setGenerateType(GenerateOption.GenerateType.xls);
        }

        @Test
        void toCommandLineArgs() {
            final GenerateOption target = GenerateOptionTest.this.createTarget();
            final Option.CommandLineArgs result = target.toCommandLineArgs();
            final Option.Arg generateType = result.getArg("-generateType");
            assertEquals(GenerateOptionTest.this.src.getGenerateType().toString(), generateType.value());
            assertEquals(Arrays.stream(GenerateOption.GenerateType.values())
                    .map(Enum::toString)
                    .toList(), generateType.attribute().selectOption());
            assertEquals(GenerateOptionTest.this.src.getUnit().toString(), result.getArg("-unit").value());
            assertNull(result.getArg("-sqlFilePrefix"));
            assertNull(result.getArg("-sqlFileSuffix"));
            assertNull(result.getArg("-commit"));
            assertNull(result.getArg("-outputEncoding"));
            assertEquals(GenerateOptionTest.this.src.getTemplate(), result.getArg("-template").value());
            final Option.Arg srcType = result.getArg("-src.srcType");
            assertEquals(GenerateOptionTest.this.src.getSrcData().getSrcType().toString(), srcType.value());
            assertEquals(Arrays.stream(DataSourceType.values())
                    .filter(it -> it != DataSourceType.none)
                    .map(Enum::toString)
                    .toList(), srcType.attribute().selectOption());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getUseJdbcMetaData(), result.getArg("-src.useJdbcMetaData").value());
            assertEquals(GenerateOptionTest.this.src.getSrcData().getLoadData(), result.getArg("-src.loadData").value());
            assertEquals(GenerateOptionTest.this.src.getTemplateOption().getEncoding(), result.getArg("-template.encoding").value());
        }
    }
}