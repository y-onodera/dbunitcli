package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;

public record TemplateRenderOption(
        String prefix
        , String encoding
        , String templateParameterAttribute
        , char templateVarStart
        , char templateVarStop
        , boolean formulaProcess
        , File templateGroup
) implements ComparableDataSetParamOption {

    public TemplateRenderOption(final String prefix, final TemplateRenderDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getEncoding()) ? dto.getEncoding() : System.getProperty("file.encoding")
                , dto.getTemplateParameterAttribute() != null ? dto.getTemplateParameterAttribute() : "param"
                , Strings.isNotEmpty(dto.getTemplateVarStart()) ? dto.getTemplateVarStart().charAt(0) : '$'
                , Strings.isNotEmpty(dto.getTemplateVarStop()) ? dto.getTemplateVarStop().charAt(0) : '$'
                , !Strings.isNotEmpty(dto.getFormulaProcess()) || Boolean.parseBoolean(dto.getFormulaProcess())
                , Strings.isNotEmpty(dto.getTemplateGroup()) ? FileResources.searchInOrderWorkspace(dto.getTemplateGroup()) : null
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-encoding", this.encoding)
                .putFile("-templateGroup", this.templateGroup)
                .put("-templateParameterAttribute", this.templateParameterAttribute)
                .put("-templateVarStart", this.templateVarStart)
                .put("-templateVarStop", this.templateVarStop);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setSTTemplateLoader(this.getTemplateRender());
    }

    public TemplateRender getTemplateRender() {
        return TemplateRender.builder()
                .setTemplateGroup(this.templateGroup)
                .setTemplateVarStart(this.templateVarStart)
                .setTemplateVarStop(this.templateVarStop)
                .setTemplateParameterAttribute(this.templateParameterAttribute)
                .setEncoding(this.encoding())
                .build();
    }

}
