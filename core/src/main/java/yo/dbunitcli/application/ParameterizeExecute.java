package yo.dbunitcli.application;

import yo.dbunitcli.dataset.Parameter;

import java.util.Map;

public class ParameterizeExecute {

    public static void main(String[] args) throws Exception {
        ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        int row = 1;
        for (Map<String, Object> param : options.loadParams()) {
            options.createCommand().exec(options, new Parameter(row, param));
            row++;
        }
    }

}
