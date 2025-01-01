package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Option {

    default String getPrefix() {
        return "";
    }

    default CommandLineArgs toCommandLineArgs() {
        return this.toCommandLineArgsBuilder().build();
    }

    CommandLineArgsBuilder toCommandLineArgsBuilder();

    enum ParamType {
        TEXT, ENUM, FLG, FILE, DIR, FILE_OR_DIR,
    }

    enum FilterType {
        ANY, INCLUDE, EXCLUDE
    }

    record Filter<T extends Enum<?>>(FilterType type, T[] target) implements Predicate<T> {

        public static <T extends Enum<?>> Filter<T> any() {
            return new Filter<>(FilterType.ANY, null);
        }

        @SafeVarargs
        public static <T extends Enum<?>> Filter<T> include(final T... target) {
            return new Filter<>(FilterType.INCLUDE, target);
        }

        @SafeVarargs
        public static <T extends Enum<?>> Filter<T> exclude(final T... target) {
            return new Filter<>(FilterType.EXCLUDE, target);
        }

        @Override
        public boolean test(final T t) {
            return switch (this.type()) {
                case ANY -> true;
                case EXCLUDE -> !List.of(this.target).contains(t);
                case INCLUDE -> List.of(this.target).contains(t);
            };
        }
    }

    record CommandLineArgs(String prefix, Map<String, Arg> options, Map<String, CommandLineArgs> subComponents) {
        public Map<String, Object> toMap() {
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("prefix", this.prefix);
            final List<Map<String, Object>> elements = new ArrayList<>();
            this.options.forEach((key, value) -> {
                final Map<String, Object> element = new LinkedHashMap<>();
                element.put("name", key.replace(Strings.isNotEmpty(this.prefix) ? "-" + this.prefix + "." : "-", ""));
                element.put("attribute", value.attribute());
                element.put("value", value.value());
                elements.add(element);
            });
            result.put("elements", elements);
            this.subComponents.forEach((key, value) -> result.put(key, value.toMap()));
            return result;
        }

        public String[] toArgs(final boolean containDefaultValue) {
            return this.toList(containDefaultValue).toArray(new String[0]);
        }

        public List<String> toList(final boolean containDefaultValue) {
            final List<String> result = new ArrayList<>();
            for (final String key : this.keySet()) {
                if (containDefaultValue || this.hasValue(key)) {
                    result.add(key + "=" + this.get(key));
                }
            }
            return result;
        }

        public List<String> keySet() {
            final List<String> results = new ArrayList<>(this.options.keySet());
            this.subComponents.forEach((key, value) -> results.addAll(value.keySet()));
            return results;
        }

        public Arg getArg(final String key) {
            return this.subComponents.values()
                    .stream()
                    .filter(it -> it.keySet().contains(key))
                    .map(it -> it.getArg(key))
                    .findFirst()
                    .orElseGet(() -> this.getArgFromOptions(key));
        }

        public String get(final String key) {
            return this.subComponents.values()
                    .stream()
                    .filter(it -> it.keySet().contains(key))
                    .map(it -> it.get(key))
                    .findFirst()
                    .orElseGet(() -> this.getFromOptions(key));
        }

        public boolean hasValue(final String key) {
            return !Optional.ofNullable(this.get(key)).orElse("").isEmpty();
        }

        private Arg getArgFromOptions(final String key) {
            if (this.options.containsKey(this.withPrefix(key))) {
                return this.options.get(this.withPrefix(key));
            } else if (this.options.containsKey(key)) {
                return this.options.get(key);
            }
            return null;
        }

        private String getFromOptions(final String key) {
            if (this.options.containsKey(this.withPrefix(key))) {
                return this.options.get(this.withPrefix(key)).value();
            } else if (this.options.containsKey(key)) {
                return this.options.get(key).value();
            }
            return "";
        }

        private String withPrefix(final String key) {
            if (Optional.ofNullable(this.prefix).orElse("").isEmpty() || key.startsWith("-" + this.prefix + ".")) {
                return key;
            }
            return key.replace("-", "-" + this.prefix + ".");
        }

    }

    class CommandLineArgsBuilder {

        private final String prefix;

        private final Map<String, Arg> options = new LinkedHashMap<>();

        private final Map<String, CommandLineArgs> subComponents = new LinkedHashMap<>();

        public CommandLineArgsBuilder() {
            this("");
        }

        public CommandLineArgsBuilder(final String prefix) {
            this.prefix = prefix;
        }

        public CommandLineArgs build() {
            return new CommandLineArgs(this.prefix, new LinkedHashMap<>(this.options), new LinkedHashMap<>(this.subComponents));
        }

        public void putAll(final CommandLineArgs other) {
            other.options.keySet()
                    .forEach(it -> {
                        final Arg entry = other.getArg(it);
                        this.put(it, entry.value, entry.attribute());
                    });
            other.subComponents.forEach(this::addComponent);
        }

        public String get(final String key) {
            return this.options.get(this.withPrefix(key)).value;
        }

        public CommandLineArgsBuilder addComponent(final String name, final CommandLineArgs subComponent) {
            this.subComponents.put(name, subComponent);
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final char value) {
            this.put(key, value, false);
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final boolean value) {
            this.put(key, Boolean.toString(value), new Attribute(ParamType.FLG, false));
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final char value, final boolean required) {
            this.put(key, String.valueOf(value), new Attribute(ParamType.TEXT, required));
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final String value) {
            this.put(key, value, false);
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final String value, final boolean required) {
            this.put(key, value, new Attribute(ParamType.TEXT, required));
            return this;
        }

        public CommandLineArgsBuilder putFile(final String key, final File value) {
            this.putFile(key, value, false);
            return this;
        }

        public CommandLineArgsBuilder putFile(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : this.getSlashSeparatorPath(value), new Attribute(ParamType.FILE, required));
            return this;
        }

        public CommandLineArgsBuilder putDir(final String key, final File value) {
            this.putDir(key, value, false);
            return this;
        }

        public CommandLineArgsBuilder putDir(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : this.getSlashSeparatorPath(value), new Attribute(ParamType.DIR, required));
            return this;
        }

        public CommandLineArgsBuilder putFileOrDir(final String key, final File value, final boolean required) {
            this.put(key, value == null ? "" : this.getSlashSeparatorPath(value), new Attribute(ParamType.FILE_OR_DIR, required));
            return this;
        }

        public <T extends Enum<?>> CommandLineArgsBuilder put(final String key, final T value, final Class<T> type) {
            this.put(key, value, type, false);
            return this;
        }

        public <T extends Enum<?>> CommandLineArgsBuilder put(final String key, final T value, final Class<T> type, final boolean required) {
            this.put(key, value, type, Filter.any(), required);
            return this;
        }

        public <T extends Enum<?>> CommandLineArgsBuilder put(final String key, final T value, final Class<T> type, final Filter<T> filter, final boolean required) {
            this.put(key, value == null ? "" : value.toString(), new Attribute(ParamType.ENUM,
                    Arrays.stream(type.getEnumConstants())
                            .filter(filter)
                            .map(Object::toString)
                            .collect(Collectors.toCollection(ArrayList::new))
                    , required)
            );
            return this;
        }

        public CommandLineArgsBuilder put(final String key, final String value, final Attribute type) {
            this.options.put(this.withPrefix(key), new Arg(Optional.ofNullable(value).orElse(""), type));
            return this;
        }

        public CommandLineArgsBuilder remove(final String key) {
            this.options.remove(key);
            return this;
        }

        private String getSlashSeparatorPath(final File value) {
            return value.getPath().replaceAll("\\\\", "/");
        }

        private String withPrefix(final String key) {
            if (Optional.ofNullable(this.prefix).orElse("").isEmpty() || key.startsWith("-" + this.prefix + ".")) {
                return key;
            }
            return key.replace("-", "-" + this.prefix + ".");
        }
    }

    record Arg(String value, Attribute attribute) {
    }

    record Attribute(ParamType type
            , ArrayList<String> selectOption
            , boolean required) {

        public Attribute(final ParamType type, final boolean required) {
            this(type, new ArrayList<>(), required);
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
