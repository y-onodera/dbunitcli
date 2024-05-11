package yo.dbunitcli.application.option;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public interface Option<T> {

    default String getPrefix() {
        return "";
    }

    CommandLineArgs toCommandLineArgs();

    enum ParamType {
        TEXT, ENUM, FILE, DIR, FILE_OR_DIR,
    }

    class CommandLineArgs {

        private final Map<String, Map<String, Attribute>> options = new HashMap<>();

        private final ArrayList<String> keys = new ArrayList<>();

        private final String prefix;

        public CommandLineArgs() {
            this("");
        }

        public CommandLineArgs(final String prefix) {
            this.prefix = prefix;
        }

        public List<String> toList(final boolean containNoValue) {
            final List<String> result = new ArrayList<>();
            for (final String key : this.keySet()) {
                if (containNoValue || this.hasValue(key)) {
                    result.add(key + "=" + this.get(key));
                }
            }
            return result;
        }

        public void putAll(final CommandLineArgs other) {
            other.keySet()
                    .forEach(it -> {
                        final Map.Entry<String, Attribute> entry = other.getColumn(it);
                        this.put(it, entry.getKey(), entry.getValue());
                    });

        }

        public void put(final String key, final char value) {
            this.put(key, value, false);
        }

        public void put(final String key, final boolean value) {
            this.put(key, Boolean.toString(value), false);
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
            this.options.put(this.withPrefix(key), new HashMap<>() {{
                this.put(Optional.ofNullable(value).orElse(""), type);
            }});
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
