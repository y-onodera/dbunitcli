package yo.dbunitcli.application.cli;

public interface ArgumentMapper {

    String[] map(String[] args, String prefix);
}
