package yo.dbunitcli.application;

import com.google.common.io.Files;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class GenerateOption extends ConvertOption {

    @Option(name = "-template", usage = "template file. data driven target argument", required = true)
    private File template;

    @Option(name = "-resultPath", usage = "Path to generate file", required = true)
    private String resultPath;

    public GenerateOption() {
        this(Parameter.NONE);
    }

    public GenerateOption(Parameter param) {
        super(param);
    }

    public String resultPath(Map<String, Object> param) {
        ST resultPath = new ST(this.getResultPath(), '$', '$');
        param.forEach(resultPath::add);
        return resultPath.render();
    }

    public ST getTemplate(Map<String, Object> param) throws IOException {
        ST result =new ST(this.templateString(), '$', '$');
        param.forEach(result::add);
        return result;
    }

    public String templateString() throws IOException {
        return Files.asCharSource(this.template, Charset.forName(getEncoding()))
                .read();
    }

    public String getResultPath() {
        return this.resultPath;
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        super.assertDirectoryExists(parser);
        if (!this.template.exists() || !this.template.isFile()) {
            throw new CmdLineException(parser, this.template + " is not exist file"
                    , new IllegalArgumentException(this.template.toString()));
        }
    }
}
