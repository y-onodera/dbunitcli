package yo.dbunitcli.application.component;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.NamedOptionDef;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateOption {

    private final String defaultEncoding;

    @Option(name = "-template", usage = "template file. generate file convert outputEncoding")
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

    public TemplateOption(String encoding) throws CmdLineException {
        this.defaultEncoding = encoding;
    }

    /**
     * @param args
     * @return args exclude this option is parsed
     * @throws CmdLineException
     */
    public String[] parseArgument(String[] args) throws CmdLineException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CmdLineParser parser = new CmdLineParser(this);
        Method expand = CmdLineParser.class.getDeclaredMethod("expandAtFiles", String[].class);
        expand.setAccessible(true);
        String[] expandArgs = (String[]) expand.invoke(parser, (Object) args);
        List<String> templateArgs = Arrays.stream(expandArgs)
                .filter(it -> {
                    for (OptionHandler handler : parser.getOptions()) {
                        if (it.startsWith(((NamedOptionDef) handler.option).name())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        parser.parseArgument(templateArgs);
        return Arrays.stream(expandArgs)
                .filter(it -> !templateArgs.contains(it))
                .toArray(String[]::new);
    }

    public String getTemplateParameterAttribute() {
        return templateParameterAttribute;
    }

    public File getTemplate() {
        return this.template;
    }

    public String getTemplateEncoding() {
        return Strings.isNullOrEmpty(this.templateEncoding) ? this.defaultEncoding : this.templateEncoding;
    }

    public void setTemplateParameterAttribute(String templateParameterAttribute) {
        this.templateParameterAttribute = templateParameterAttribute;
    }

    public void setTemplateVarStart(char templateVarStart) {
        this.templateVarStart = templateVarStart;
    }

    public void setTemplateVarStop(char templateVarStop) {
        this.templateVarStop = templateVarStop;
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
