package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class TemplateRenderOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-encoding", usage = "template file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-template", usage = "template file")
    private File template;

    @Option(name = "-templateEncoding", usage = "template file encoding.default is encoding option")
    private String templateEncoding;

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

    public File getTemplate() {
        return this.template;
    }

    public String getTemplateEncoding() {
        return Strings.isNullOrEmpty(this.templateEncoding) ? this.encoding : this.templateEncoding;
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
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = super.expandOption(args);
        result.put("-templateEncoding", this.encoding);
        result.putFile("-template", this.template);
        result.putFile("-templateGroup", this.templateGroup);
        result.put("-templateParameterAttribute", this.templateParameterAttribute);
        result.put("-templateVarStart", this.templateVarStart);
        result.put("-templateVarStop", this.templateVarStop);
        return result;
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        if (this.template != null) {
            if (!template.exists() || !template.isFile()) {
                throw new CmdLineException(parser, template + " is not exist file"
                        , new IllegalArgumentException(template.toString()));
            }
        }
    }

}
