package yo.dbunitcli.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.command.Compare;
import yo.dbunitcli.application.command.Convert;
import yo.dbunitcli.application.command.Generate;
import yo.dbunitcli.application.command.Parameterize;
import yo.dbunitcli.application.command.Run;
import yo.dbunitcli.application.command.Scaffold;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineOptionValidationTest {

    @TempDir
    Path workspace;

    @BeforeEach
    void setUp() {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, this.workspace.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
    }

    private void createFile(final String path) throws IOException {
        final Path file = this.workspace.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "");
    }

    private List<String> validate(final Supplier<? extends Option> option) {
        final List<List<Option.Violation>> result = new ArrayList<>();
        FileResources.runInContext(() -> result.add(option.get().validate()));
        return messages(result.getFirst());
    }

    private static List<String> messages(final List<Option.Violation> violations) {
        return violations.stream().map(Option.Violation::message).toList();
    }

    @Nested
    class Helper {

        private final TestOption option = new TestOption("new", List.of());

        @Test
        void violationHasPrefixedOptionNames() {
            assertEquals(List.of(List.of("-new.cmd")), this.option.validateRequired("-cmd", "").stream()
                                                                  .map(Option.Violation::options).toList());
            assertEquals(List.of(List.of("-new.cmdParam", "-new.template"))
                    , this.option.validateExclusive("-cmdParam", "a", "-template", "b").stream()
                                 .map(Option.Violation::options).toList());
        }

        @Test
        void requiredEmptyIsInvalid() {
            assertEquals(List.of("Missing required option: '-new.cmd=<cmd>'"), messages(this.option.validateRequired("-cmd", "")));
        }

        @Test
        void requiredSpecifiedIsValid() {
            assertEquals(List.of(), messages(this.option.validateRequired("-cmd", "convert")));
        }

        @Test
        void fileNotExistsIsInvalid() {
            assertEquals(List.of("Invalid value for option '-new.setting': file not found: 'notExists.json'")
                    , messages(this.option.validateFileExists("-setting", "notExists.json", Option.BaseDir.SETTING)));
        }

        @Test
        void fileExistsIsValid() throws IOException {
            CommandLineOptionValidationTest.this.createFile("resources/setting/a.json");
            assertEquals(List.of(), messages(this.option.validateFileExists("-setting", "a.json", Option.BaseDir.SETTING)));
        }

        @Test
        void fileEmptyIsNotChecked() {
            assertEquals(List.of(), messages(this.option.validateFileExists("-setting", "", Option.BaseDir.SETTING)));
        }

        @Test
        void settingIsCommaSeparated() throws IOException {
            CommandLineOptionValidationTest.this.createFile("resources/setting/a.json");
            assertEquals(List.of("Invalid value for option '-new.setting': file not found: 'b.json'")
                    , messages(this.option.validateFilesExist("-setting", "a.json, b.json", Option.BaseDir.SETTING)));
        }
    }

    @Nested
    class Exec {

        @Test
        void validateBeforeExecute() {
            final TestCommand command = new TestCommand();
            final OptionValidationException ex = assertThrows(OptionValidationException.class
                    , () -> command.exec(new TestOption("", List.of(new Option.Violation(List.of(), "error1")
                            , new Option.Violation(List.of(), "error2")))));
            assertEquals("error1\nerror2", ex.getMessage());
            assertEquals(0, command.executed.size());
        }

        @Test
        void executeWhenValid() {
            final TestCommand command = new TestCommand();
            final TestOption option = new TestOption("", List.of());
            command.exec(option);
            assertEquals(List.of(option), command.executed);
        }
    }

    @Nested
    class ParameterizeOption {

        @Test
        void cmdParamAndTemplateIsExclusive() throws IOException {
            CommandLineOptionValidationTest.this.createFile("template.txt");
            assertEquals(List.of("Error: -cmdParam=<cmdParam>, -template=<template> are mutually exclusive (specify only one)")
                    , this.validate("-cmd=convert", "-cmdParam=param.txt", "-template=template.txt"));
        }

        @Test
        void cmdIsRequired() {
            assertEquals(List.of("Missing required option: '-cmd=<cmd>'"), this.validate("-cmdParam=param.txt"));
        }

        @Test
        void templateNotExistsIsInvalid() {
            assertEquals(List.of("Invalid value for option '-template': file not found: 'template.txt'")
                    , this.validate("-cmd=convert", "-template=template.txt"));
        }

        @Test
        void cmdParamIsNotChecked() {
            assertEquals(List.of(), this.validate("-cmd=convert", "-cmdParam=$param.name$.txt"));
        }

        @Test
        void unknownCmdIsInvalid() {
            assertEquals(List.of("Invalid value for option '-cmd': expected one of "
                            + "[compare, convert, generate, run, parameterize] but was 'scaffold'")
                    , this.validate("-cmd=scaffold"));
        }

        @Test
        void templateVarStartMustBeSingleChar() {
            assertEquals(List.of("Invalid value for option '-template.templateVarStart': expected a single character but was '<<'")
                    , this.validate("-cmd=convert", "-template.templateVarStart=<<"));
        }

        @Test
        void templateCmdIsNotChecked() {
            assertEquals(List.of(), this.validate("-cmd=$param.cmd$"));
        }

        private List<String> validate(final String... args) {
            return CommandLineOptionValidationTest.this.validate(() -> new Parameterize().parseOption(args));
        }
    }

    @Nested
    class DataSetLoadOption {

        @Test
        void srcNotExistsIsInvalid() {
            assertEquals(List.of("Invalid value for option '-src.src': file not found: 'notExists'"), this.validate("-src.src=notExists"));
        }

        @Test
        void srcEmptyIsValid() {
            assertEquals(List.of(), this.validate());
        }

        @Test
        void subOptionIsValidated() throws IOException {
            CommandLineOptionValidationTest.this.createFile("dataset/a.xlsx");
            assertEquals(List.of("Invalid value for option '-src.xlsxSchema': file not found: 'schema.json'")
                    , this.validate("-src.src=dataset", "-src.srcType=xlsx", "-src.xlsxSchema=schema.json"));
        }

        @Test
        void regRequiresDataSplitAndHeaderSplitOrHeaderName() {
            assertEquals(List.of("Missing required option: '-src.regDataSplit=<regDataSplit>'"
                            , "Error: Missing required argument (specify one of these): "
                                    + "(-src.regHeaderSplit=<regHeaderSplit> | -src.headerName=<headerName>)")
                    , this.validate("-src.srcType=reg"));
            assertEquals(List.of(), this.validate("-src.srcType=reg", "-src.regDataSplit=,", "-src.headerName=A,B"));
        }

        @Test
        void fixedRequiresFixedLengthAndHeaderName() {
            assertEquals(List.of("Missing required option: '-src.fixedLength=<fixedLength>'"
                            , "Missing required option: '-src.headerName=<headerName>'")
                    , this.validate("-src.srcType=fixed"));
            assertEquals(List.of(), this.validate("-src.srcType=fixed", "-src.fixedLength=3,5", "-src.headerName=A,B"));
        }

        @Test
        void unsupportedEncodingIsInvalid() {
            assertEquals(List.of("Invalid value for option '-src.encoding': unsupported encoding: 'XXX'")
                    , this.validate("-src.encoding=XXX"));
        }

        @Test
        void invalidRegexIsInvalid() {
            assertEquals(List.of("Invalid value for option '-src.regInclude': invalid regex: '[' (Unclosed character class)")
                    , this.validate("-src.regInclude=["));
        }

        @Test
        void tableResultRequiresJdbcUrl() throws IOException {
            assertEquals(List.of("Error: Missing required argument (specify one of these): "
                            + "(-result.jdbcUrl=<jdbcUrl> | -result.jdbcProperties=<jdbcProperties>)")
                    , this.validate("-result.resultType=table"));
            CommandLineOptionValidationTest.this.createFile("resources/jdbc/noUrl.properties");
            assertEquals(List.of("Invalid value for option '-result.jdbcProperties': url is not defined in 'noUrl.properties'")
                    , this.validate("-result.resultType=table", "-result.jdbcProperties=noUrl.properties"));
            assertEquals(List.of(), this.validate("-result.resultType=table", "-result.jdbcUrl=jdbc:h2:mem:test"));
        }

        @Test
        void excelTableMustBeSheetOrBook() {
            assertEquals(List.of("Invalid value for option '-result.excelTable': expected one of [SHEET, BOOK] but was 'PAGE'")
                    , this.validate("-result.resultType=xlsx", "-result.excelTable=PAGE"));
        }

        @Test
        void fixedLengthTypeMustBeCharOrByte() {
            assertEquals(List.of("Invalid value for option '-src.fixedLengthType': "
                            + "expected one of [CHAR, BYTE] (case-insensitive) but was 'bytes'")
                    , this.validate("-src.srcType=fixed", "-src.fixedLength=3", "-src.headerName=A"
                            , "-src.fixedLengthType=bytes"));
            assertEquals(List.of(), this.validate("-src.srcType=fixed", "-src.fixedLength=3", "-src.headerName=A"
                    , "-src.fixedLengthType=BYTE"));
            assertEquals(List.of("Invalid value for option '-result.fixedLengthType': "
                            + "expected one of [CHAR, BYTE] (case-insensitive) but was 'x'")
                    , this.validate("-result.resultType=fixed", "-result.fixedLengthType=x"));
        }

        @Test
        void delimiterMustBeSingleCharAfterUnescape() {
            assertEquals(List.of("Invalid value for option '-src.delimiter': expected a single character but was ';;'")
                    , this.validate("-src.delimiter=;;"));
            assertEquals(List.of(), this.validate("-src.delimiter=\\t"));
        }

        @Test
        void formatResultRequiresFormat() {
            assertEquals(List.of("Missing required option: '-result.format=<format>'")
                    , this.validate("-result.resultType=format"));
        }

        @Test
        void resultFixedColumnDefIsValidated() {
            assertEquals(List.of("Invalid value for option '-result.fixedColumnDef': file not found: 'def.json'")
                    , this.validate("-result.resultType=fixed", "-result.fixedColumnDef=def.json"));
        }

        private List<String> validate(final String... args) {
            return CommandLineOptionValidationTest.this.validate(() -> new Convert().parseOption(args));
        }
    }

    @Nested
    class GenerateOption {

        @Test
        void templateIsRequired() {
            assertEquals(List.of("Missing required option: '-template=<template>'"), this.validate("-generateType=txt"));
        }

        @Test
        void fixedTemplateDoesNotRequireTemplate() {
            assertEquals(List.of(), this.validate("-generateType=settings"));
        }

        @Test
        void fixedColumnDefAlignMustBeLeftOrRight() {
            assertEquals(List.of("Invalid value for option '-align': expected one of [left, right] but was 'center'")
                    , this.validate("-generateType=fixedColumnDef", "-align=center"));
        }

        @Test
        void sqlRequiresOp() {
            assertEquals(List.of("Missing required option: '-op=<op>'"), this.validate("-generateType=sql"));
            assertEquals(List.of(), this.validate("-generateType=sql", "-op=INSERT"));
        }

        private List<String> validate(final String... args) {
            return CommandLineOptionValidationTest.this.validate(() -> new Generate().parseOption(args));
        }
    }

    @Nested
    class ScaffoldOption {

        @Test
        void noTargetIsValid() {
            assertEquals(List.of(), CommandLineOptionValidationTest.this.validate(
                    () -> new Scaffold().parseOption(new String[]{"-result=out"})));
        }

        @Test
        void unknownCommandTypeIsInvalid() {
            assertEquals(List.of("Invalid value for option '-commandType': expected one of "
                            + "[compare, convert, generate, parameterize, run, scaffold] but was 'foo'")
                    , CommandLineOptionValidationTest.this.validate(
                            () -> new Scaffold().parseOption(new String[]{"-target=parameter", "-commandType=foo"})));
        }
    }

    @Nested
    class CompareOption {

        @Test
        void excludedAreasMustMatchFormat() {
            assertEquals(List.of("Invalid value for option '-image.excludedAreas': expected [x1,y1,x2,y2] repeated but was '[1,2,3]'")
                    , this.validate("-targetType=image", "-image.excludedAreas=[1,2,3]"));
            assertEquals(List.of(), this.validate("-targetType=image", "-image.excludedAreas=[0,0,10,10][5.5,5,20,20]"));
        }

        private List<String> validate(final String... args) {
            return CommandLineOptionValidationTest.this.validate(() -> new Compare().parseOption(args));
        }
    }

    @Nested
    class RunOption {

        @Test
        void baseDirNotExistsIsInvalid() {
            assertEquals(List.of("Invalid value for option '-baseDir': file not found: 'notExists'")
                    , this.validate("-scriptType=cmd", "-baseDir=notExists"));
        }

        @Test
        void sqlRequiresJdbcUrl() {
            assertEquals(List.of("Error: Missing required argument (specify one of these): "
                            + "(-jdbc.jdbcUrl=<jdbcUrl> | -jdbc.jdbcProperties=<jdbcProperties>)")
                    , this.validate("-scriptType=sql"));
            assertEquals(List.of(), this.validate("-scriptType=cmd"));
        }

        private List<String> validate(final String... args) {
            return CommandLineOptionValidationTest.this.validate(() -> new Run().parseOption(args));
        }
    }

    @Nested
    class CliOutput {

        private final ByteArrayOutputStream err = new ByteArrayOutputStream();

        private PrintStream originalErr;

        private String originalNoExit;

        @BeforeEach
        void setUp() {
            this.originalNoExit = System.setProperty("dbunitcli.noExit", "true");
            this.originalErr = System.err;
            System.setErr(new PrintStream(this.err, true, StandardCharsets.UTF_8));
        }

        @AfterEach
        void tearDown() {
            System.setErr(this.originalErr);
            // surefireが全テストに-Ddbunitcli.noExit=trueを渡しているため、消さずに元へ戻す
            if (this.originalNoExit == null) {
                System.clearProperty("dbunitcli.noExit");
            } else {
                System.setProperty("dbunitcli.noExit", this.originalNoExit);
            }
        }

        @Test
        void validationErrorPrintsMessageOnlyWithUsageExitCode() {
            final RuntimeException ex = assertThrows(RuntimeException.class
                    , () -> new Parameterize().exec(new String[]{"-cmdParam=a", "-template=b"}));
            assertEquals("2", ex.getMessage());
            assertEquals(String.join("\n"
                    , "Missing required option: '-cmd=<cmd>'"
                    , "Error: -cmdParam=<cmdParam>, -template=<template> are mutually exclusive (specify only one)"
                    , "Invalid value for option '-template': file not found: 'b'")
                    + System.lineSeparator(), this.err.toString(StandardCharsets.UTF_8));
        }

        @Test
        void parseErrorShowsPrefixedOptionName() {
            final RuntimeException ex = assertThrows(RuntimeException.class
                    , () -> new Convert().exec(new String[]{"-src.srcType=foo"}));
            assertEquals("2", ex.getMessage());
            assertTrue(this.err.toString(StandardCharsets.UTF_8)
                               .startsWith("Invalid value for option '-src.srcType': expected one of ")
                    , this.err.toString(StandardCharsets.UTF_8));
        }

        @Test
        void parseErrorShowsOptionNameAsSpecified() {
            assertThrows(RuntimeException.class, () -> new Convert().exec(new String[]{"-srcType=foo"}));
            assertTrue(this.err.toString(StandardCharsets.UTF_8)
                               .startsWith("Invalid value for option '-srcType': expected one of ")
                    , this.err.toString(StandardCharsets.UTF_8));
        }
    }

    record TestOption(String prefix, List<Option.Violation> errors) implements CommandLineOption<CommandDto> {

        @Override
        public String getPrefix() {
            return this.prefix;
        }

        @Override
        public List<Option.Violation> validate() {
            return this.errors;
        }

        @Override
        public CommandDto toDto() {
            return new CommandDto();
        }

        @Override
        public Parameter parameter() {
            return Parameter.none();
        }

        @Override
        public ParametersBuilder toParametersBuilder() {
            return new ParametersBuilder(this.prefix);
        }
    }

    static class TestCommand implements Command<CommandDto, TestOption> {

        final List<TestOption> executed = new ArrayList<>();

        @Override
        public void execute(final TestOption options) {
            this.executed.add(options);
        }

        @Override
        public CommandDto createDto(final String[] args) {
            return new CommandDto();
        }

        @Override
        public TestOption parseOption(final String resultFile, final CommandDto dto, final Parameter param) {
            return new TestOption("", List.of());
        }
    }
}
