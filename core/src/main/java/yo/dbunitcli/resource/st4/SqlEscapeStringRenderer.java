package yo.dbunitcli.resource.st4;

import org.stringtemplate.v4.StringRenderer;
import yo.dbunitcli.Strings;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SqlEscapeStringRenderer extends StringRenderer {

    @Override
    public String toString(final Object o, final String formatString, final Locale locale) {
        if (formatString == null) {
            return o.toString();
        } else if (formatString.equals("camelToSnake")) {
            return Strings.camelToSnake(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("camelToUpperSnake")) {
            return Strings.camelToSnake(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("snakeToCamel")) {
            return Strings.snakeToCamel(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("snakeToUpperCamel")) {
            return Strings.snakeToCamel(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("camelToKebab")) {
            return Strings.camelToKebab(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("camelToUpperKebab")) {
            return Strings.camelToKebab(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("kebabToCamel")) {
            return Strings.kebabToCamel(o.toString(), Character::toLowerCase);
        } else if (formatString.equals("kebabToUpperCamel")) {
            return Strings.kebabToCamel(o.toString(), Character::toUpperCase);
        } else if (formatString.equals("escapeSql")) {
            return this.toClob(o.toString());
        } else if (formatString.equals("jexlExp")) {
            return "${" + o.toString() + "}";
        } else if (formatString.equals("ST4Exp")) {
            return "$" + o.toString() + "$";
        }
        return super.toString(o, formatString, locale);
    }

    private static final int LITERAL_MAX_BYTES = 4000;

    private String toClob(final String value) {
        final int length = SqlEscapeStringRenderer.escape(value).getBytes(StandardCharsets.UTF_8).length;
        if (value.contains("\r\n")) {
            return this.joinLines(value.split("\r\n"), " || CHR(13) || CHR(10) || ", length);
        } else if (value.contains("\n")) {
            return this.joinLines(value.split("\n"), " || CHR(10) || ", length);
        }
        return this.quoted(value, length);
    }

    private String joinLines(final String[] lines, final String separator, final int length) {
        return Arrays.stream(lines)
                .map(it -> this.quoted(it, length))
                .collect(Collectors.joining(separator));
    }

    /**
     * Oracleの文字列リテラルは4000バイトまでのため、超える場合はTO_CLOBで分割して連結する。
     * 分割はエスケープ前の文字列で行い、''の組やサロゲートペアを分けない
     */
    private String quoted(final String value, final int length) {
        if (length < LITERAL_MAX_BYTES) {
            return "'" + SqlEscapeStringRenderer.escape(value) + "'";
        }
        final List<String> chunks = new ArrayList<>();
        final StringBuilder chunk = new StringBuilder();
        int chunkBytes = 0;
        for (int i = 0; i < value.length(); ) {
            final int codePoint = value.codePointAt(i);
            final String escaped = codePoint == '\'' ? "''" : new String(Character.toChars(codePoint));
            final int bytes = escaped.getBytes(StandardCharsets.UTF_8).length;
            if (chunkBytes + bytes > LITERAL_MAX_BYTES) {
                chunks.add(chunk.toString());
                chunk.setLength(0);
                chunkBytes = 0;
            }
            chunk.append(escaped);
            chunkBytes += bytes;
            i += Character.charCount(codePoint);
        }
        if (!chunk.isEmpty() || chunks.isEmpty()) {
            chunks.add(chunk.toString());
        }
        return chunks.stream()
                .map(it -> "TO_CLOB('" + it + "')")
                .collect(Collectors.joining(" || "));
    }

    private static String escape(final String value) {
        return value.replace("'", "''");
    }

}
