package yo.dbunitcli;

import java.util.Optional;

public enum Strings {
    SINGLETON;

    public static boolean isEmpty(final String s) {
        return Optional.ofNullable(s).orElse("").isEmpty();
    }

    public static boolean isNotEmpty(final String s) {
        return !Strings.isEmpty(s);
    }

}
