package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.Map;

public class TemplateRenderOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-encoding", description = "template file encoding")
    private String encoding = System.getProperty("file.encoding");

    @CommandLine.Option(names = "-templateGroup", description = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @CommandLine.Option(names = "-templateParameterAttribute", description = "attributeName that is used to for access parameter in StringTemplate expression default 'param'.")
    private String templateParameterAttribute = "param";

    @CommandLine.Option(names = "-templateVarStart", description = "StringTemplate expression start char.default '$'")
    private char templateVarStart = '$';

    @CommandLine.Option(names = "-templateVarStop", description = "StringTemplate expression stop char.default '$'\"")
    private char templateVarStop = '$';

    @CommandLine.Option(names = "-formulaProcess", description = "default true.if false xlsx output use LowerMemory but cellRef in formula isn't along with row increase")
    private String formulaProcess = "true";

    public TemplateRenderOption(final String prefix) {
        super(prefix);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setSTTemplateLoader(this.getTemplateRender());
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
    public void setUpComponent(final String[] expandArgs) {
        if (this.templateGroup != null) {
            if (!this.templateGroup.exists() || !this.templateGroup.isFile()) {
                throw new AssertionError(this.templateGroup + " is not exist file"
                        , new IllegalArgumentException(this.templateGroup.toString()));
            }
        }
    }

}
