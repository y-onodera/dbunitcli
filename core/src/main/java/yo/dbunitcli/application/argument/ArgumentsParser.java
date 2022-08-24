package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface ArgumentsParser {

    /**
     * @param args option
     * @return args exclude this option is parsed
     */
    default CmdLineParser parseArgument(String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            String[] targetArgs = this.getArgumentMapper().map(args,this.getPrefix(),parser);
            parser.parseArgument(this.filterArguments(parser, targetArgs));
            setUpComponent(parser, targetArgs);
        } catch (CmdLineException cx) {
            System.out.println("usage:");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            throw cx;
        }
        return parser;
    }

    default Collection<String> filterArguments(CmdLineParser parser, String[] expandArgs) {
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

        private final ArrayList<String> keys = Lists.newArrayList();

        private final String prefix;

        private final Map<String, String> args;

        public OptionParam(String prefix, Map<String, String> args) {
            this.prefix = prefix;
            this.args = args;
        }

        public void putAll(OptionParam other) {
            other.options
                    .rowKeySet()
                    .forEach(it -> {
                        Map.Entry<String, Attribute> entry = other.options.row(it).entrySet().iterator().next();
                        put(it, entry.getKey(), entry.getValue());
                    });

        }

        public void put(String key, char value) {
            this.put(key, value, false);
        }

        public void put(String key, char value, boolean required) {
            this.put(key, String.valueOf(value), new Attribute(ParamType.TEXT, required));
        }

        public void put(String key, String value) {
            this.put(key, value, false);
        }

        public void put(String key, String value, boolean required) {
            this.put(key, value, new Attribute(ParamType.TEXT, required));
        }

        public void putFile(String key, File value) {
            this.putFile(key, value, false);
        }

        public void putFile(String key, File value, boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.FILE, required));
        }

        public void putDir(String key, File value) {
            this.putDir(key, value, false);
        }

        public void putDir(String key, File value, boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.DIR, required));
        }

        public void putFileOrDir(String key, File value, boolean required) {
            this.put(key, value == null ? "" : value.getPath(), new Attribute(ParamType.FILE_OR_DIR, required));
        }

        public <T extends Enum<?>> void put(String key, T value, Class<T> type) {
            this.put(key, value, type, false);
        }

        public <T extends Enum<?>> void put(String key, T value, Class<T> type, boolean required) {
            this.put(key, value == null ? "" : value.toString(), new Attribute(ParamType.ENUM,
                    Arrays.stream(type.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.toCollection(ArrayList::new))
                    , required)
            );
        }

        public void put(String key, String value, Attribute type) {
            if (Strings.isNullOrEmpty(this.args.get(withPrefix(key)))) {
                this.options.put(withPrefix(key), this.args.getOrDefault(key, Strings.nullToEmpty(value)), type);
            } else {
                this.options.put(withPrefix(key), this.args.getOrDefault(withPrefix(key), value), type);
            }
            this.keys.add(withPrefix(key));
        }

        public Iterable<String> keySet() {
            return this.keys;
        }

        public Map.Entry<String, Attribute> getColumn(String key) {
            if (this.options.containsRow(withPrefix(key))) {
                return this.options.row(withPrefix(key)).entrySet().iterator().next();
            } else if (this.options.containsRow(key)) {
                return this.options.row(key).entrySet().iterator().next();
            }
            return null;
        }

        public String get(String key) {
            if (this.options.containsRow(withPrefix(key))) {
                return this.options.row(withPrefix(key)).keySet().iterator().next();
            } else if (this.options.containsRow(key)) {
                return this.options.row(key).keySet().iterator().next();
            }
            return "";
        }

        public boolean hasValue(String key) {
            return !Strings.isNullOrEmpty(this.get(key));
        }

        protected String withPrefix(String key) {
            if (Strings.isNullOrEmpty(this.prefix) || key.startsWith("-" + this.prefix + ".")) {
                return key;
            }
            return key.replace("-", "-" + this.prefix + ".");
        }

    }

    class Attribute {

        private final ParamType type;

        private final ArrayList<String> selectOption;

        private final boolean required;

        public Attribute(ParamType type, boolean required) {
            this(type, Lists.newArrayList(), required);
        }

        public Attribute(ParamType type, ArrayList<String> selectOption, boolean required) {
            this.type = type;
            this.selectOption = selectOption;
            this.required = required;
        }

        public ParamType getType() {
            return type;
        }

        public ArrayList<String> getSelectOption() {
            return selectOption;
        }

        public boolean isRequired() {
            return required;
        }
    }

    enum ParamType {
        TEXT, ENUM, FILE, DIR, FILE_OR_DIR,
    }

}
