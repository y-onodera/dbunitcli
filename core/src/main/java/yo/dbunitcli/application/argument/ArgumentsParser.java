package yo.dbunitcli.application.argument;

import com.google.common.collect.HashBasedTable;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public interface ArgumentsParser {

    /**
     * @param args option
     * @return args exclude this option is parsed
     */
    default CmdLineParser parseArgument(final String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);
        try {
            final String[] targetArgs = this.getArgumentMapper().map(args, this.getPrefix(), parser);
            parser.parseArgument(this.filterArguments(parser, targetArgs));
            this.setUpComponent(parser, targetArgs);
        } catch (final CmdLineException cx) {
            System.out.println("usage:");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            throw new AssertionError(cx);
        }
        return parser;
    }

    default Collection<String> filterArguments(final CmdLineParser parser, final String[] expandArgs) {
        return this.getArgumentMapper().mapFilterArgument(this.getArgumentFilter()
                        .filterArguments(this.getPrefix(), parser, expandArgs)
                , this.getPrefix(), parser, expandArgs);
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

    OptionParam createOptionParam(Map<String, String> args);

    void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException;

    class OptionParam {

        private final HashBasedTable<String, String, Attribute> options = HashBasedTable.create();

        private final ArrayList<String> keys = new ArrayList<>();

        private final String prefix;

        private final Map<String, String> args;

        public OptionParam(final String prefix, final Map<String, String> args) {
            this.prefix = prefix;
            this.args = args;
        }

        public void putAll(final OptionParam other) {
            other.options
                    .rowKeySet()
                    .forEach(it -> {
                        final Map.Entry<String, Attribute> entry = other.options.row(it).entrySet().iterator().next();
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
                this.options.put(this.withPrefix(key), this.args.getOrDefault(key, Optional.ofNullable(value).orElse("")), type);
            } else {
                this.options.put(this.withPrefix(key), this.args.getOrDefault(this.withPrefix(key), value), type);
            }
            this.keys.add(this.withPrefix(key));
        }

        public Iterable<String> keySet() {
            return this.keys;
        }

        public Map.Entry<String, Attribute> getColumn(final String key) {
            if (this.options.containsRow(this.withPrefix(key))) {
                return this.options.row(this.withPrefix(key)).entrySet().iterator().next();
            } else if (this.options.containsRow(key)) {
                return this.options.row(key).entrySet().iterator().next();
            }
            return null;
        }

        public String get(final String key) {
            if (this.options.containsRow(this.withPrefix(key))) {
                return this.options.row(this.withPrefix(key)).keySet().iterator().next();
            } else if (this.options.containsRow(key)) {
                return this.options.row(key).keySet().iterator().next();
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

    enum ParamType {
        TEXT, ENUM, FILE, DIR, FILE_OR_DIR,
    }

}
