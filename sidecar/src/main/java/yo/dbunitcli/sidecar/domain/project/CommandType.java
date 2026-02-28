package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.application.*;

public enum CommandType {
    compare {
        @Override
        public Command<?, ?> getCommand() {
            return new Compare();
        }
    }, convert {
        @Override
        public Command<?, ?> getCommand() {
            return new Convert();
        }
    }, generate {
        @Override
        public Command<?, ?> getCommand() {
            return new Generate();
        }
    }, parameterize {
        @Override
        public Command<?, ?> getCommand() {
            return new Parameterize();
        }
    }, run {
        @Override
        public Command<?, ?> getCommand() {
            return new Run();
        }
    };

    public abstract Command<?, ?> getCommand();
}
