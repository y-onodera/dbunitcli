package yo.dbunitcli;

import java.util.Optional;
import java.util.function.Function;

public enum Strings {
    SINGLETON;

    public static boolean isEmpty(final String s) {
        return Optional.ofNullable(s).orElse("").isEmpty();
    }

    public static boolean isNotEmpty(final String s) {
        return !Strings.isEmpty(s);
    }

    public static String camelToSnake(final String camel, final Function<Character, Character> caseFormat) {
        return Strings.camelToOtherFormat(camel, '_', caseFormat);
    }

    public static String camelToKebab(final String camel, final Function<Character, Character> caseFormat) {
        return Strings.camelToOtherFormat(camel, '-', caseFormat);
    }

    public static String snakeToCamel(final String snake, final Function<Character, Character> caseFormat) {
        return Strings.otherToCamel(snake, '_', caseFormat);
    }

    public static String kebabToCamel(final String kebab, final Function<Character, Character> caseFormat) {
        return Strings.otherToCamel(kebab, '-', caseFormat);
    }

    private static String otherToCamel(final String kebab, final char x, final Function<Character, Character> caseFormat) {
        if (kebab.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(kebab.length() + kebab.length());
        for (int i = 0; i < kebab.length(); i++) {
            final char c = kebab.charAt(i);
            if (c == x) {
                sb.append((i + 1) < kebab.length() ? Character.toUpperCase(kebab.charAt(++i)) : "");
            } else {
                sb.append(sb.length() == 0 ? caseFormat.apply(c) : Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private static String camelToOtherFormat(final String camel, final char separator, final Function<Character, Character> caseFormat) {
        if (camel.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(camel.length() + camel.length());
        for (int i = 0; i < camel.length(); i++) {
            final char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                boolean addSeparator = sb.length() != 0;
                if (i + 1 < camel.length()) {
                    addSeparator = addSeparator && Character.isLowerCase(camel.charAt(i + 1));
                } else if (i != 0 && i + 1 == camel.length()) {
                    addSeparator = addSeparator && Character.isLowerCase(camel.charAt(i - 1));
                }
                sb.append(addSeparator ? separator : "").append(caseFormat.apply(c));
            } else {
                sb.append(caseFormat.apply(c));
            }
        }
        return sb.toString();
    }

}
