package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.Map;

public class TemplateRenderOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-encoding", usage = "template file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-templateGroup", usage = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @Option(name = "-templateParameterAttribute", usage = "attributeName that is used to for access parameter in StringTemplate expression default 'param'.")
    private String templateParameterAttribute = "param";

    @Option(name = "-templateVarStart", usage = "StringTemplate expression start char.default '$'")
    private char templateVarStart = '$';

    @Option(name = "-templateVarStop", usage = "StringTemplate expression stop char.default '$'\"")
    private char templateVarStop = '$';

    public TemplateRenderOption(String prefix) {
        super(prefix);
    }


    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setSTTemplateLoader(this.getTemplateRender());
    }

    public String getTemplateParameterAttribute() {
        return templateParameterAttribute;
    }

    public String getTemplateEncoding() {
        return this.encoding;
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
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-encoding", this.encoding);
        result.putFile("-templateGroup", this.templateGroup);
        result.put("-templateParameterAttribute", this.templateParameterAttribute);
        result.put("-templateVarStart", this.templateVarStart);
        result.put("-templateVarStop", this.templateVarStop);
        return result;
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        if (this.templateGroup != null) {
            if (!this.templateGroup.exists() || !this.templateGroup.isFile()) {
                throw new CmdLineException(parser, this.templateGroup + " is not exist file"
                        , new IllegalArgumentException(this.templateGroup.toString()));
            }
        }
    }

}
