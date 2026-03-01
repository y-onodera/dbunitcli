package yo.dbunitcli.application;

public interface CommandType {
    Command<?, ?> getCommand();
}
