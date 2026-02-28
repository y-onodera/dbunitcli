package yo.dbunitcli.application.cli;

public interface ArgumentFunction {

    String[] apply(String[] args, String prefix);
}
