package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.annotation.Controller;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.ParameterUnit;
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
        final Map<String, Object> result = super.serialize(parameters);
        if (Type.generate.getCommand().parseOption(parameters.args()) instanceof final GenerateOption option
                && option.generateType() != null) {
            final ParameterUnit fixedUnit = option.generateType().strategy().fixedUnit();
            if (fixedUnit != null) {
                result.put("fixedUnit", fixedUnit.name());
            }
        }
        return result;
    }

}
