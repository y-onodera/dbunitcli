package yo.dbunitcli.application.command;

import yo.dbunitcli.application.Command;
import yo.dbunitcli.application.CommandType;

public enum Type implements CommandType {
    compare, convert, generate, parameterize, run;

    @Override
    public Command<?, ?> getCommand() {
        return switch (this) {
            case compare -> new Compare();
            case convert -> new Convert();
            case generate -> new Generate();
            case parameterize -> new Parameterize();
            case run -> new Run();
        };
    }
}
