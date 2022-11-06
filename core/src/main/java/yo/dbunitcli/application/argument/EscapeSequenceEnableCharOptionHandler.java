package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.CharOptionHandler;
import org.kohsuke.args4j.spi.Setter;

public class EscapeSequenceEnableCharOptionHandler extends CharOptionHandler {

    public EscapeSequenceEnableCharOptionHandler(final CmdLineParser parser, final OptionDef option, final Setter<? super Character> setter) {
        super(parser, option, setter);
    }

    @Override
    protected Character parse(final String argument) throws NumberFormatException, CmdLineException {
        return super.parse(argument.replace("\\b", "\b")
                .replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\f", "\f")
        );
    }
}
