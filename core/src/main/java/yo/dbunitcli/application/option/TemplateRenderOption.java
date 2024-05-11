package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;

public class TemplateRenderOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final String encoding;
    private final String templateParameterAttribute;
    private final char templateVarStart;
    private final char templateVarStop;
    private final String formulaProcess;
    private final File templateGroup;

    public TemplateRenderOption(final String prefix, final TemplateRenderDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getEncoding())) {
            this.encoding = dto.getEncoding();
        } else {
            this.encoding = System.getProperty("file.encoding");
        }
        if (Strings.isNotEmpty(dto.getTemplateGroup())) {
            this.templateGroup = new File(dto.getTemplateGroup());
        } else {
            this.templateGroup = null;
        }
        if (dto.getTemplateParameterAttribute() != null) {
            this.templateParameterAttribute = dto.getTemplateParameterAttribute();
        } else {
            this.templateParameterAttribute = "param";
        }
        if (Strings.isNotEmpty(dto.getTemplateVarStart())) {
            this.templateVarStart = dto.getTemplateVarStart().charAt(0);
        } else {
            this.templateVarStart = '$';
        }
        if (Strings.isNotEmpty(dto.getTemplateVarStop())) {
            this.templateVarStop = dto.getTemplateVarStop().charAt(0);
        } else {
            this.templateVarStop = '$';
        }
        if (Strings.isNotEmpty(dto.getFormulaProcess())) {
            this.formulaProcess = dto.getFormulaProcess();
        } else {
            this.formulaProcess = "true";
        }
        if (this.templateGroup != null) {
            if (!this.templateGroup.exists() || !this.templateGroup.isFile()) {
                throw new AssertionError(this.templateGroup + " is not exist file"
                        , new IllegalArgumentException(this.templateGroup.toString()));
            }
        }
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
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-encoding", this.encoding);
        result.putFile("-templateGroup", this.templateGroup);
        result.put("-templateParameterAttribute", this.templateParameterAttribute);
        result.put("-templateVarStart", this.templateVarStart);
        result.put("-templateVarStop", this.templateVarStop);
        return result;
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
