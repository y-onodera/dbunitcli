package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public interface Option {

    ParametersBuilder toParametersBuilder();

    default String getPrefix() {
        return "";
    }

    default Parameters toParameters() {
        return this.toParametersBuilder().build();
    }

    /**
     * 展開済みの値を検証し、違反内容を返す。デフォルト値の有無やsrcType等による使われ方の違いは各Optionが考慮する
     */
    default List<Violation> validate() {
        return List.of();
    }

    // 違反の文言はpicocliのエラー表示に揃える
    default List<Violation> validateRequired(final String key, final String value) {
        if (Strings.isNotEmpty(value)) {
            return List.of();
        }
        return List.of(new Violation(List.of(this.optionName(key))
                , "Missing required option: '" + this.optionLabel(key) + "'"));
    }

    default List<Violation> validateExclusive(final String key1, final String value1
            , final String key2, final String value2) {
        if (Strings.isEmpty(value1) || Strings.isEmpty(value2)) {
            return List.of();
        }
        return List.of(new Violation(List.of(this.optionName(key1), this.optionName(key2))
                , "Error: " + this.optionLabel(key1) + ", " + this.optionLabel(key2)
                + " are mutually exclusive (specify only one)"));
    }

    default List<Violation> validateRequiredEither(final String key1, final String value1
            , final String key2, final String value2) {
        if (Strings.isNotEmpty(value1) || Strings.isNotEmpty(value2)) {
            return List.of();
        }
        return List.of(new Violation(List.of(this.optionName(key1), this.optionName(key2))
                , "Error: Missing required argument (specify one of these): ("
                + this.optionLabel(key1) + " | " + this.optionLabel(key2) + ")"));
    }

    default List<Violation> validateOneOf(final String key, final String value, final Collection<String> candidates) {
        if (Strings.isEmpty(value) || candidates.contains(value)) {
            return List.of();
        }
        return this.invalidValue(key, "expected one of " + candidates + " but was '" + value + "'");
    }

    default List<Violation> validateOneOfIgnoreCase(final String key, final String value
            , final Collection<String> candidates) {
        if (Strings.isEmpty(value) || candidates.stream().anyMatch(value::equalsIgnoreCase)) {
            return List.of();
        }
        return this.invalidValue(key, "expected one of " + candidates + " (case-insensitive) but was '" + value + "'");
    }

    // 型変換は値を使う所で行うため、変換できることをここで確かめる（文言はpicocliの型変換エラーに揃える）
    default List<Violation> validateBoolean(final String key, final String value) {
        if (Strings.isEmpty(value) || "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return List.of();
        }
        return this.invalidValue(key, "'" + value + "' is not a boolean");
    }

    default List<Violation> validateInteger(final String key, final String value) {
        return this.validateInteger(key, value, Integer.MIN_VALUE);
    }

    default List<Violation> validateInteger(final String key, final String value, final int min) {
        if (Strings.isEmpty(value)) {
            return List.of();
        }
        try {
            if (Integer.parseInt(value.trim()) >= min) {
                return List.of();
            }
            return this.invalidValue(key, "must be " + min + " or more but was '" + value + "'");
        } catch (final NumberFormatException ex) {
            return this.invalidValue(key, "'" + value + "' is not an int");
        }
    }

    default List<Violation> validateDecimal(final String key, final String value) {
        if (Strings.isEmpty(value)) {
            return List.of();
        }
        try {
            Double.parseDouble(value);
            return List.of();
        } catch (final NumberFormatException ex) {
            return this.invalidValue(key, "'" + value + "' is not a double");
        }
    }

    /**
     * カンマ区切りの整数（-fixedLength等）
     */
    default List<Violation> validateIntegers(final String key, final String value) {
        if (Strings.isEmpty(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                     .filter(it -> !it.trim().matches("-?\\d+"))
                     .findFirst()
                     .map(it -> this.invalidValue(key, "'" + it + "' in '" + value + "' is not an int"))
                     .orElse(List.of());
    }

    default List<Violation> validateSingleChar(final String key, final String value) {
        if (Strings.isEmpty(value) || value.length() == 1) {
            return List.of();
        }
        return this.invalidValue(key, "expected a single character but was '" + value + "'");
    }

    default List<Violation> validateEncoding(final String key, final String value) {
        if (Strings.isEmpty(value)) {
            return List.of();
        }
        try {
            if (Charset.isSupported(value)) {
                return List.of();
            }
        } catch (final IllegalCharsetNameException ex) {
            // 文字コード名として不正なものも未対応として扱う
        }
        return this.invalidValue(key, "unsupported encoding: '" + value + "'");
    }

    default List<Violation> validatePattern(final String key, final String value) {
        if (Strings.isEmpty(value)) {
            return List.of();
        }
        try {
            Pattern.compile(value);
            return List.of();
        } catch (final PatternSyntaxException ex) {
            return this.invalidValue(key, "invalid regex: '" + value + "' (" + ex.getDescription() + ")");
        }
    }

    default List<Violation> invalidValue(final String key, final String reason) {
        return List.of(new Violation(List.of(this.optionName(key))
                , "Invalid value for option '" + this.optionName(key) + "': " + reason));
    }

    /**
     * 指定されているときだけ、実行時と同じ探し方で存在を確認する
     */
    default List<Violation> validateFileExists(final String key, final String value, final BaseDir baseDir) {
        if (Strings.isEmpty(value) || baseDir.search(value).exists()) {
            return List.of();
        }
        return this.invalidValue(key, "file not found: '" + value + "'");
    }

    /**
     * カンマ区切りで複数指定できるオプション（-setting等）の各ファイルの存在を確認する
     */
    default List<Violation> validateFilesExist(final String key, final String values, final BaseDir baseDir) {
        if (Strings.isEmpty(values)) {
            return List.of();
        }
        return Arrays.stream(values.split(","))
                     .flatMap(it -> this.validateFileExists(key, it.trim(), baseDir).stream())
                     .toList();
    }

    /**
     * picocliと同じ「-name=&lt;name&gt;」形式の表示名
     */
    default String optionLabel(final String key) {
        return this.optionName(key) + "=<" + key.substring(1) + ">";
    }

    default String optionName(final String key) {
        return Option.withPrefix(this.getPrefix(), key);
    }

    /**
     * -nameをprefix付きの-prefix.nameにする（prefixが空か、既にprefix付きならそのまま）
     */
    static String withPrefix(final String prefix, final String key) {
        if (Strings.isEmpty(prefix) || key.startsWith("-" + prefix + ".")) {
            return key;
        }
        return key.replace("-", "-" + prefix + ".");
    }

    @SafeVarargs
    static List<Violation> concat(final List<Violation>... violations) {
        return Arrays.stream(violations).flatMap(List::stream).toList();
    }

    enum ParamType {
        TEXT, ENUM, FLG, FILE, DIR, FILE_OR_DIR,
    }

    enum BaseDir {
        WORKSPACE, COMMAND_PARAM, DATASET, RESULT, SETTING, TEMPLATE, PARAMETERIZE_TEMPLATE, JDBC, XLSX_SCHEMA, FIXED_COLUMN_DEF;

        /**
         * 各オプションが実行時に探すのと同じ順でpathを解決する
         */
        public File search(final String path) {
            return switch (this) {
                case WORKSPACE, COMMAND_PARAM -> FileResources.searchWorkspace(path);
                case DATASET -> FileResources.searchDatasetBase(path);
                case RESULT -> FileResources.resultDir(path);
                case SETTING -> FileResources.searchSetting(path);
                case TEMPLATE -> FileResources.searchTemplate(path);
                case PARAMETERIZE_TEMPLATE -> FileResources.searchParameterizeTemplate(path);
                case JDBC -> FileResources.searchJdbc(path);
                case XLSX_SCHEMA -> FileResources.searchXlsxSchema(path);
                case FIXED_COLUMN_DEF -> FileResources.searchFixedColumnDef(path);
            };
        }
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

    record Parameters(String prefix, Map<String, Arg> options, Map<String, Parameters> subComponents) {

        public Map<String, Object> serialize() {
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("prefix", this.prefix);
            this.options.forEach((key, value) -> {
                final Map<String, Object> element = new HashMap<>();
                element.put("name", key.replace(Strings.isNotEmpty(this.prefix) ? "-" + this.prefix + "." : "-", ""));
                element.put("attribute", value.attribute());
                element.put("value", value.value());
                result.put(element.get("name").toString(), element);
            });
            this.subComponents.forEach((key, value) -> result.put(key, value.serialize()));
            return result;
        }

        public String[] toArgs(final boolean containDefaultValue) {
            return this.toList(containDefaultValue).toArray(new String[0]);
        }

        public List<String> parameterizeArgs(final boolean containDefaultValue) {
            return this.toList(containDefaultValue, key -> key + "=$param." + this.templateEscape(key) + "$");
        }

        public Map<String, ?> toMap(final boolean containDefaultValue) {
            return this.toList(containDefaultValue, key -> key).stream()
                       .collect(Collectors.toMap(this::templateEscape, this::get));
        }

        public List<String> toList(final boolean containDefaultValue) {
            return this.toList(containDefaultValue, key -> key + "=" + this.get(key));
        }

        public List<String> toList(final boolean containDefaultValue, Function<String, String> function) {
            final List<String> result = new ArrayList<>();
            for (final String key : this.keySet()) {
                if (containDefaultValue || this.hasValue(key)) {
                    result.add(function.apply(key));
                }
            }
            return result;
        }

        public List<String> keySet() {
            final List<String> results = new ArrayList<>(this.options.keySet());
            this.subComponents.forEach((_, value) -> results.addAll(value.keySet()));
            return results;
        }

        public boolean hasValue(final String key) {
            return !Optional.of(this.get(key)).orElse("").isEmpty();
        }

        public String get(final String key) {
            return this.subComponents.values().stream().filter(it -> it.keySet().contains(key)).map(it -> it.get(key))
                                     .findFirst().orElseGet(() -> this.getFromOptions(key));
        }

        public Arg getArg(final String key) {
            return this.subComponents.values().stream().filter(it -> it.keySet().contains(key))
                                     .map(it -> it.getArg(key)).findFirst()
                                     .orElseGet(() -> this.getArgFromOptions(key));
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
            return Option.withPrefix(this.prefix, key);
        }

        private String templateEscape(final String key) {
            return (key.startsWith("-") ? key.substring(1) : key).replace(".", "_");
        }
    }

    class ParametersBuilder {

        private final String prefix;

        private final Map<String, Arg> options = new LinkedHashMap<>();

        private final Map<String, Parameters> subComponents = new LinkedHashMap<>();

        public ParametersBuilder() {
            this("");
        }

        public ParametersBuilder(final String prefix) {
            this.prefix = prefix;
        }

        public Parameters build() {
            return new Parameters(this.prefix, new LinkedHashMap<>(this.options),
                                  new LinkedHashMap<>(this.subComponents));
        }

        public String get(final String key) {
            return this.options.get(this.withPrefix(key)).value;
        }

        public ParametersBuilder putAll(final Parameters other) {
            other.options.keySet().forEach(it -> {
                if (!this.options.containsKey(it)) {
                    final Arg entry = other.getArg(it);
                    this.put(it, entry.value, entry.attribute());
                }
            });
            other.subComponents.forEach(this::addComponent);
            return this;
        }

        public ParametersBuilder addComponent(final String name, final Parameters subComponent) {
            this.subComponents.put(name, subComponent);
            return this;
        }

        public ParametersBuilder put(final String key, final char value) {
            return this.put(key, value, false);
        }

        public ParametersBuilder put(final String key, final boolean value) {
            return this.putFlag(key, Boolean.toString(value));
        }

        /**
         * 真偽値を文字列のまま持つオプション用。不正な値もそのまま出し、検証は{@link Option#validate()}で行う
         */
        public ParametersBuilder putFlag(final String key, final String value) {
            return this.put(key, value, new Attribute(ParamType.FLG, false));
        }

        public ParametersBuilder put(final String key, final char value, final boolean required) {
            return this.put(key, String.valueOf(value), new Attribute(ParamType.TEXT, required));
        }

        public ParametersBuilder put(final String key, final String value) {
            return this.put(key, value, false);
        }

        public ParametersBuilder put(final String key, final String value, final boolean required) {
            return this.put(key, value, new Attribute(ParamType.TEXT, required));
        }

        public ParametersBuilder putFile(final String key, final String value, final BaseDir defaultPath) {
            return this.putFile(key, value, false, defaultPath);
        }

        public ParametersBuilder putFile(final String key, final String value, final boolean required,
                                         final BaseDir defaultPath) {
            return this.putFile(key, value, new Attribute(ParamType.FILE, required, defaultPath));
        }

        public ParametersBuilder putDir(final String key, final String value, final BaseDir defaultPath) {
            return this.putFile(key, value, new Attribute(ParamType.DIR, false, defaultPath));
        }

        public ParametersBuilder putFileOrDir(final String key, final String value, final boolean required,
                                              final BaseDir defaultPath) {
            return this.putFile(key, value, new Attribute(ParamType.FILE_OR_DIR, required, defaultPath));
        }

        public ParametersBuilder putFile(final String key, final String value, final Attribute attribute) {
            return this.put(key, Strings.isEmpty(value) ? "" : this.getSlashSeparatorPath(new File(value)), attribute);
        }

        public <T extends Enum<?>> ParametersBuilder put(final String key, final T value, final Class<T> type) {
            return this.put(key, value, type, false);
        }

        public <T extends Enum<?>> ParametersBuilder put(final String key, final T value, final Class<T> type,
                                                         final boolean required) {
            return this.put(key, value, type, Filter.any(), required);
        }

        public <T extends Enum<?>> ParametersBuilder put(final String key, final T value, final Class<T> type,
                                                         final Filter<T> filter, final boolean required) {
            return this.put(key, value == null ? "" : value.toString(), new Attribute(ParamType.ENUM, Arrays.stream(
                    type.getEnumConstants()).filter(filter).map(Object::toString).collect(
                    Collectors.toCollection(ArrayList::new)), required));
        }

        public ParametersBuilder put(final String key, final String value, final Attribute type) {
            this.options.put(this.withPrefix(key), new Arg(Optional.ofNullable(value).orElse(""), type));
            return this;
        }

        public ParametersBuilder remove(final String key) {
            this.options.remove(key);
            return this;
        }

        private String getSlashSeparatorPath(final File value) {
            return value.getPath().replaceAll("\\\\", "/");
        }

        private String withPrefix(final String key) {
            return Option.withPrefix(this.prefix, key);
        }
    }

    record Arg(String value, Attribute attribute) {
    }

    /**
     * 検証の違反。optionsは違反に関わるオプション名（prefix付き）で、GUIでは該当する入力欄にmessageを表示する
     */
    record Violation(List<String> options, String message) {
    }

    record Attribute(ParamType type, ArrayList<String> selectOption, boolean required, BaseDir defaultPath) {

        public Attribute(final ParamType type, final boolean required) {
            this(type, new ArrayList<>(), required, BaseDir.WORKSPACE);
        }

        public Attribute(final ParamType type, final boolean required, final BaseDir defaultPath) {
            this(type, new ArrayList<>(), required, defaultPath);
        }

        public Attribute(final ParamType type, final ArrayList<String> selectOption, final boolean required) {
            this(type, selectOption, required, BaseDir.WORKSPACE);
        }
    }

}