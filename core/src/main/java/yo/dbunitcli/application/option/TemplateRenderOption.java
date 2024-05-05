package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.Map;

public class TemplateRenderOption implements ComparableDataSetParamOption {

    private final String prefix;
    private String encoding = System.getProperty("file.encoding");

    private File templateGroup;

    private String templateParameterAttribute = "param";

    private char templateVarStart = '$';

    private char templateVarStop = '$';

    private String formulaProcess = "true";

    public TemplateRenderOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setSTTemplateLoader(this.getTemplateRender());
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-encoding", this.encoding);
        result.putFile("-templateGroup", this.templateGroup);
        result.put("-templateParameterAttribute", this.templateParameterAttribute);
        result.put("-templateVarStart", this.templateVarStart);
        result.put("-templateVarStop", this.templateVarStop);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        this.setUpComponent(dto.getTemplateRender());
    }

    public void setUpComponent(final TemplateRenderDto dto) {
        if (Strings.isNotEmpty(dto.getEncoding())) {
            this.encoding = dto.getEncoding();
        }
        if (Strings.isNotEmpty(dto.getTemplateGroup())) {
            this.templateGroup = new File(dto.getTemplateGroup());
        }
        if (dto.getTemplateParameterAttribute() != null) {
            this.templateParameterAttribute = dto.getTemplateParameterAttribute();
        }
        if (Strings.isNotEmpty(dto.getTemplateVarStart())) {
            this.templateVarStart = dto.getTemplateVarStart().charAt(0);
        }
        if (Strings.isNotEmpty(dto.getTemplateVarStop())) {
            this.templateVarStop = dto.getTemplateVarStop().charAt(0);
        }
        if (Strings.isNotEmpty(dto.getFormulaProcess())) {
            this.formulaProcess = dto.getFormulaProcess();
        }
        if (this.templateGroup != null) {
            if (!this.templateGroup.exists() || !this.templateGroup.isFile()) {
                throw new AssertionError(this.templateGroup + " is not exist file"
                        , new IllegalArgumentException(this.templateGroup.toString()));
            }
        }
    }

    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    public String getTemplateEncoding() {
        return this.encoding;
    }

    public boolean isFormulaProcess() {
        return Boolean.parseBoolean(this.formulaProcess);
    }

    public TemplateRender getTemplateRender() {
        return TemplateRender.builder()
                .setTemplateGroup(this.templateGroup)
                .setTemplateVarStart(this.templateVarStart)
                .setTemplateVarStop(this.templateVarStop)
                .setTemplateParameterAttribute(this.templateParameterAttribute)
                .setEncoding(this.getTemplateEncoding())
                .build();
    }

}
