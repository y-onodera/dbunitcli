package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.nio.charset.Charset;
import java.util.List;

public record TemplateRenderOption(
        String prefix
        , String encoding
        , String templateParameterAttribute
        , String templateVarStart
        , String templateVarStop
        , String formulaProcess
        , String evaluateFormulas
        , String forceFormulaRecalc
        , String fastFormulaProcess
        , String deleteBlankCells
        , String templateGroup
) implements ComparableDataSetParamOption {

    public TemplateRenderOption(final String prefix, final TemplateRenderDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getEncoding()) ? dto.getEncoding() : Charset.defaultCharset().displayName()
                , dto.getTemplateParameterAttribute() != null ? dto.getTemplateParameterAttribute() : "param"
                , Strings.isNotEmpty(dto.getTemplateVarStart()) ? dto.getTemplateVarStart() : "$"
                , Strings.isNotEmpty(dto.getTemplateVarStop()) ? dto.getTemplateVarStop() : "$"
                , Strings.isNotEmpty(dto.getFormulaProcess()) ? dto.getFormulaProcess() : "true"
                , Strings.isNotEmpty(dto.getEvaluateFormulas()) ? dto.getEvaluateFormulas() : "true"
                , Strings.isNotEmpty(dto.getForceFormulaRecalc()) ? dto.getForceFormulaRecalc() : "false"
                , Strings.isNotEmpty(dto.getFastFormulaProcess()) ? dto.getFastFormulaProcess() : "false"
                , Strings.isNotEmpty(dto.getDeleteBlankCells()) ? dto.getDeleteBlankCells() : "false"
                , dto.getTemplateGroup()
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .put("-encoding", this.encoding)
                .putFile("-templateGroup", this.templateGroup, BaseDir.TEMPLATE)
                .put("-templateParameterAttribute", this.templateParameterAttribute)
                .put("-templateVarStart", this.templateVarStart)
                .put("-templateVarStop", this.templateVarStop);
    }

    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateFileExists("-templateGroup", this.templateGroup, BaseDir.TEMPLATE)
                , this.validateEncoding("-encoding", this.encoding)
                , this.validateSingleChar("-templateVarStart", this.templateVarStart)
                , this.validateSingleChar("-templateVarStop", this.templateVarStop)
                , this.validateBoolean("-formulaProcess", this.formulaProcess)
                , this.validateBoolean("-evaluateFormulas", this.evaluateFormulas)
                , this.validateBoolean("-forceFormulaRecalc", this.forceFormulaRecalc)
                , this.validateBoolean("-fastFormulaProcess", this.fastFormulaProcess)
                , this.validateBoolean("-deleteBlankCells", this.deleteBlankCells));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setSTTemplateLoader(this.getTemplateRender());
    }

    public TemplateRender getTemplateRender() {
        return TemplateRender.builder()
                .setEncoding(this.encoding())
                .setTemplateGroup(FileResources.searchTemplate(this.templateGroup))
                .setTemplateParameterAttribute(this.templateParameterAttribute)
                .setTemplateVarStart(this.templateVarStart.charAt(0))
                .setTemplateVarStop(this.templateVarStop.charAt(0))
                .build();
    }

}
