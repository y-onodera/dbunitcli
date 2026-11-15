package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.util.Map;

@Controller("/generate")
public class GenerateController extends AbstractCommandController {

    public GenerateController(final Workspace workspace) {
        super(workspace);
    }

    @Override
    protected Type getCommandType() {
        return Type.generate;
    }

    /**
     * 固定成果物型は-unitを引数に持たないため、出力単位をfixedUnitとして返す（GUIのテンプレート変数の候補に使う）
     */
    @Override
    protected Map<String, Object> serialize(final CommandParameters parameters) {
        final CommandLineOption<?> option = parameters.type().getCommand().parseOption(parameters.args());
        final Map<String, Object> result = option.toParameters().serialize();
        if (option instanceof final GenerateOption generate
                && generate.generateType() != null && generate.generateType().strategy().isFixedTemplate()) {
            result.put("fixedUnit", generate.unit().name());
        }
        return result;
    }

}
