package yo.dbunitcli.application.argument;

import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public interface ArgumentsParser {

    /**
     * @param args option
     */
    default void parseArgument(final String[] args) {
        final CommandLine cmdLine = new CommandLine(this);
        final String[] targetArgs = this.getArgumentMapper().map(args, this.getPrefix(), cmdLine);
        cmdLine.parseArgs(this.filterArguments(cmdLine, targetArgs));
        this.setUpComponent(targetArgs);
    }

    default String[] filterArguments(final CommandLine commandLine, final String[] expandArgs) {
        return this.getArgumentFilter()
                .filterArguments(this.getPrefix(), commandLine, expandArgs);
    }

    default ArgumentFilter getArgumentFilter() {
        return new DefaultArgumentFilter();
    }

    default ArgumentMapper getArgumentMapper() {
        return new DefaultArgumentMapper();
    }

    default String getPrefix() {
        return "";
    }

    default OptionParam createOptionParam(final String[] args) {
        final String[] expandArgs = this.getExpandArgs(args);
        this.parseArgument(expandArgs);
        return this.createOptionParam(Arrays.stream(expandArgs)
                .collect(Collectors.toMap(this.getArgumentFilter().extractKey()
                        , it -> it.replace(this.getArgumentFilter().extractKey().apply(it) + "=", "")
                )));
    }

    OptionParam createOptionParam(Map<String, String> args);

    void setUpComponent(String[] expandArgs);

    default String[] getExpandArgs(final String[] args) {
        final List<String> result = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("@")) {
                final File file = new File(arg.substring(1));
                if (!file.exists()) {
                    throw new AssertionError("file not exists :" + file.getPath());
                }
                try {
                    result.addAll(Files.readAllLines(file.toPath()));
                } catch (final IOException ex) {
                    throw new AssertionError("Failed to parse " + file, ex);
                }
            } else {
                result.add(arg);
            }
        }
        return result.toArray(new String[0]);
    }

    enum ParamType {
        TEXT, ENUM, FILE, DIR, FILE_OR_DIR,
    }

    class OptionParam {

        private final Map<String, Map<String, Attribute>> options = new HashMap<>();

        private final ArrayList<String> keys = new ArrayList<>();

        private final String prefix;

        private final Map<String, String> args;

        public OptionParam(final String prefix, final Map<String, String> args) {
            this.prefix = prefix;
            this.args = args;
        }

        public void putAll(final OptionParam other) {
            other.keySet()
                    .forEach(it -> {
                        final Map.Entry<String, Attribute> entry = other.getColumn(it);
                        this.put(it, entry.getKey(), entry.getValue());
                    });

        }

        public void put(final String key, final char value) {
            this.put(key, value, false);
        }

        public void put(final String key, final char value, final boolean required) {
            this.put(key, String.valueOf(value), new Attribute(ParamType.TEXT, required));
        }

        public void put(final String key, final String value) {
            this.put(key, value, false);
        }

        public void put(final String key, final String value, final boolean required) {
            this.put(key, value, new Attribute(ParamType.TEXT, required));
        }

        public void putFile(final String key, final File value) {
            this.putFile(key, value, false);
        }

        public void putFile(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.FILE, required));
        }

        public void putDir(final String key, final File value) {
            this.putDir(key, value, false);
        }

        public void putDir(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.DIR, required));
        }

        public void putFileOrDir(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.FILE_OR_DIR, required));
        }

        public <T extends Enum<?>> void put(final String key, final T value, final Class<T> type) {
            this.put(key, value, type, false);
        }

        public <T extends Enum<?>> void put(final String key, final T value, final Class<T> type, final boolean required) {
            this.put(key, value == null ? "" : value.toString(), new Attribute(ParamType.ENUM,
                    Arrays.stream(type.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.toCollection(ArrayList::new))
                    , required)
            );
        }

        public void put(final String key, final String value, final Attribute type) {
            if (Optional.ofNullable(this.args.get(this.withPrefix(key))).orElse("").isEmpty()) {
                this.options.put(this.withPrefix(key), new HashMap<>() {{
                    this.put(OptionParam.this.args.getOrDefault(key, Optional.ofNullable(value).orElse("")), type);
                }});
            } else {
                this.options.put(this.withPrefix(key), new HashMap<>() {{
                    this.put(OptionParam.this.args.getOrDefault(OptionParam.this.withPrefix(key), value), type);
                }});
            }
            this.keys.add(this.withPrefix(key));
        }

        public Iterable<String> keySet() {
            return this.keys;
        }

        public Map.Entry<String, Attribute> getColumn(final String key) {
            if (this.options.containsKey(this.withPrefix(key))) {
                return this.options.get(this.withPrefix(key)).entrySet().iterator().next();
            } else if (this.options.containsKey(key)) {
                return this.options.get(key).entrySet().iterator().next();
            }
            return null;
        }

        public String get(final String key) {
            if (this.options.containsKey(this.withPrefix(key))) {
                return this.options.get(this.withPrefix(key)).keySet().iterator().next();
            } else if (this.options.containsKey(key)) {
                return this.options.get(key).keySet().iterator().next();
            }
            return "";
        }

        public boolean hasValue(final String key) {
            return !Optional.ofNullable(this.get(key)).orElse("").isEmpty();
        }

        protected String withPrefix(final String key) {
            if (Optional.ofNullable(this.prefix).orElse("").isEmpty() || key.startsWith("-" + this.prefix + ".")) {
                return key;
            }
            return key.replace("-", "-" + this.prefix + ".");
        }

    }

    class Attribute {

        private final ParamType type;

        private final ArrayList<String> selectOption;

        private final boolean required;

        public Attribute(final ParamType type, final boolean required) {
            this(type, new ArrayList<>(), required);
        }

        public Attribute(final ParamType type, final ArrayList<String> selectOption, final boolean required) {
            this.type = type;
            this.selectOption = selectOption;
            this.required = required;
        }

        public ParamType getType() {
            return this.type;
        }

        public ArrayList<String> getSelectOption() {
            return this.selectOption;
        }

        public boolean isRequired() {
            return this.required;
        }
    }

}
