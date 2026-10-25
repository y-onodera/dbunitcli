package yo.dbunitcli.application;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import yo.dbunitcli.common.Parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineOptionValidationTest {

    @Test
    void noRuleIsAlwaysValid() {
        assertDoesNotThrow(() -> new TestOption(Map.of("-cmdParam", "a", "-template", "b"), null).validate());
    }

    @Nested
    class Exclusive {

        @Test
        void bothSpecifiedIsInvalid() {
            final OptionValidationException ex = assertThrows(OptionValidationException.class
                    , () -> this.option(Map.of("-cmdParam", "a", "-template", "b")).validate());
            assertTrue(ex.getMessage().contains("mutually exclusive"), ex.getMessage());
        }

        @Test
        void oneSpecifiedIsValid() {
            assertDoesNotThrow(() -> this.option(Map.of("-template", "b", "-other", "c")).validate());
        }

        @Test
        void emptyValueIsTreatedAsUnspecified() {
            assertDoesNotThrow(() -> this.option(Map.of("-cmdParam", "a", "-template", "")).validate());
        }

        private TestOption option(final Map<String, String> args) {
            return new TestOption(args, ExclusiveRule::new);
        }
    }

    @Nested
    class Dependent {

        @Test
        void dependentWithoutRequiredIsInvalid() {
            final OptionValidationException ex = assertThrows(OptionValidationException.class
                    , () -> this.option(Map.of("-new.settingEncoding", "UTF-8")).validate());
            assertTrue(ex.getMessage().contains("-new.setting"), ex.getMessage());
        }

        @Test
        void dependentWithRequiredIsValid() {
            assertDoesNotThrow(() -> this.option(Map.of("-new.setting", "a.json", "-new.settingEncoding", "UTF-8"))
                    .validate());
        }

        @Test
        void requiredOnlyIsValid() {
            assertDoesNotThrow(() -> this.option(Map.of("-new.setting", "a.json")).validate());
        }

        @Test
        void noneSpecifiedIsValid() {
            assertDoesNotThrow(() -> this.option(Map.of("-new.src", "dir")).validate());
        }

        private TestOption option(final Map<String, String> args) {
            return new TestOption(args, DependentRule::new);
        }
    }

    @Nested
    class Exec {

        @Test
        void validateBeforeExecute() {
            final TestCommand command = new TestCommand();
            assertThrows(OptionValidationException.class
                    , () -> command.exec(new TestOption(Map.of("-cmdParam", "a", "-template", "b"), ExclusiveRule::new)));
            assertEquals(0, command.executed.size());
        }

        @Test
        void executeWhenValid() {
            final TestCommand command = new TestCommand();
            final TestOption option = new TestOption(Map.of("-template", "b"), ExclusiveRule::new);
            command.exec(option);
            assertEquals(List.of(option), command.executed);
        }
    }

    static class ExclusiveRule {
        @CommandLine.ArgGroup
        Group group;

        static class Group {
            @CommandLine.Option(names = "-cmdParam")
            String cmdParam;
            @CommandLine.Option(names = "-template")
            String template;
        }
    }

    static class DependentRule {
        @CommandLine.ArgGroup(exclusive = false)
        Group group;

        static class Group {
            @CommandLine.Option(names = "-new.setting", required = true)
            String setting;
            @CommandLine.Option(names = "-new.settingEncoding")
            String settingEncoding;
        }
    }

    record TestOption(Map<String, String> args, Supplier<Object> rule) implements CommandLineOption<CommandDto> {

        @Override
        public Optional<?> validationRule() {
            return Optional.ofNullable(this.rule).map(Supplier::get);
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
            final ParametersBuilder result = new ParametersBuilder();
            new LinkedHashMap<>(this.args).forEach(result::put);
            return result;
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
            return new TestOption(Map.of(), null);
        }
    }
}
