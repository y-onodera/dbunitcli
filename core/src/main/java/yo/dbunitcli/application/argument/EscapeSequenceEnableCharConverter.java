package yo.dbunitcli.application.argument;

import picocli.CommandLine;

public class EscapeSequenceEnableCharConverter implements CommandLine.ITypeConverter<Character> {
    @Override
    public Character convert(final String s) throws Exception {
        return s.replace("\\b", "\b")
                .replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\f", "\f")
                .charAt(0);
    }
}
