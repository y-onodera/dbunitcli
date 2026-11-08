package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

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

    private static final String DEFAULT_PARAMETER_ATTRIBUTE = "param";
    /**
     * 属性名なし（カラム名で直接参照）を表す値。GUIでは空の値を送れず未指定と区別できないため、空の代わりに使う
     */
    private static final String NO_PARAMETER_ATTRIBUTE = "\"\"";
    private static final String DEFAULT_VAR_DELIMITER = "$";

    public TemplateRenderOption(final String prefix, final TemplateRenderDto dto) {
        this(prefix
                , dto.getEncoding()
                , dto.getTemplateParameterAttribute()
                , dto.getTemplateVarStart()
                , dto.getTemplateVarStop()
                , Strings.isNotEmpty(dto.getFormulaProcess()) ? dto.getFormulaProcess() : "true"
                , Strings.isNotEmpty(dto.getEvaluateFormulas()) ? dto.getEvaluateFormulas() : "true"
                , Strings.isNotEmpty(dto.getForceFormulaRecalc()) ? dto.getForceFormulaRecalc() : "false"
                , Strings.isNotEmpty(dto.getFastFormulaProcess()) ? dto.getFastFormulaProcess() : "false"
                , Strings.isNotEmpty(dto.getDeleteBlankCells()) ? dto.getDeleteBlankCells() : "false"
                , dto.getTemplateGroup()
        );
    }

    @Override
    public String encoding() {
        return EncodingOption.orDefault(this.encoding);
    }

    @Override
    public String templateParameterAttribute() {
        if (this.templateParameterAttribute == null) {
            return DEFAULT_PARAMETER_ATTRIBUTE;
        }
        return NO_PARAMETER_ATTRIBUTE.equals(this.templateParameterAttribute.trim()) ? "" : this.templateParameterAttribute;
    }

    @Override
    public String templateVarStart() {
        return Strings.orDefault(this.templateVarStart, DEFAULT_VAR_DELIMITER);
    }

    @Override
    public String templateVarStop() {
        return Strings.orDefault(this.templateVarStop, DEFAULT_VAR_DELIMITER);
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .putWithDefault("-encoding", this.encoding, EncodingOption.defaultEncoding())
                .putFile("-templateGroup", this.templateGroup, BaseDir.TEMPLATE)
                .putWithDefault("-templateParameterAttribute", this.templateParameterAttribute, DEFAULT_PARAMETER_ATTRIBUTE)
                .putWithDefault("-templateVarStart", this.templateVarStart, DEFAULT_VAR_DELIMITER)
                .putWithDefault("-templateVarStop", this.templateVarStop, DEFAULT_VAR_DELIMITER);
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
                .setTemplateParameterAttribute(this.templateParameterAttribute())
                .setTemplateVarStart(this.templateVarStart().charAt(0))
                .setTemplateVarStop(this.templateVarStop().charAt(0))
                .build();
    }

}
