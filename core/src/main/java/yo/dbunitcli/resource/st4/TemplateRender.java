package yo.dbunitcli.resource.st4;

import com.google.common.base.Strings;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.ErrorManager;
import yo.dbunitcli.resource.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TemplateRender {

    private final File templateGroup;

    private final String templateParameterAttribute;

    private final char templateVarStart;

    private final char templateVarStop;

    private final String encoding;

    public TemplateRender() {
        this(new Builder());
    }

    public TemplateRender(Builder builder) {
        this.encoding = builder.getEncoding();
        this.templateGroup = builder.getTemplateGroup();
        this.templateParameterAttribute = builder.getTemplateParameterAttribute();
        this.templateVarStart = builder.getTemplateVarStart();
        this.templateVarStop = builder.getTemplateVarStop();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEncoding() {
        return this.encoding;
    }

    public String getTemplateParameterAttribute() {
        return templateParameterAttribute;
    }

    public char getTemplateVarStart() {
        return templateVarStart;
    }

    public char getTemplateVarStop() {
        return templateVarStop;
    }

    public String render(File aFile, Map<String, Object> parameter) throws IOException {
        return this.render(Files.read(aFile, this.getEncoding()), parameter);
    }

    public String render(String target, Map<String, Object> parameter) {
        if (parameter.size() > 0) {
            return this.createST(target, parameter).render();
        }
        return target;
    }

    public String replaceParameter(String target, Map<String, Object> parameter) {
        String result = target;
        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
            result = result.replace(this.getAttributeName(entry.getKey()), entry.getValue().toString());
        }
        return result;
    }

    public ST createST(String result) {
        return new ST(this.createSTGroup(), result);
    }

    public ST createST(String target, Map<String, Object> parameter) {
        String template = this.replaceParameter(target, parameter);
        ST st = createST(template);
        if (Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
            parameter.forEach(st::add);
        } else {
            st.add(this.getTemplateParameterAttribute(), parameter);
        }
        return st;
    }

    public STGroup createSTGroup() {
        return this.createSTGroup(this.templateGroup);
    }

    public STGroup createSTGroup(File groupFile) {
        if (groupFile == null) {
            return this.createSTGroup("");
        }
        return this.createSTGroup(groupFile.getAbsolutePath());
    }

    public STGroup createSTGroup(String fileName) {
        STGroup stGroup;
        if (Strings.isNullOrEmpty(fileName)) {
            stGroup = new STGroup(this.templateVarStart, this.templateVarStop);
        } else {
            stGroup = new STGroupFile(fileName, this.templateVarStart, this.templateVarStop);
        }
        stGroup.registerRenderer(String.class, new SqlEscapeStringRenderer());
        return stGroup;
    }

    public void write(String templateString, Map<String, Object> param, File resultFile, String outputEncoding) throws IOException {
        this.write(this.createSTGroup(), templateString, param, resultFile, outputEncoding);
    }

    public void write(STGroup stGroup, String templateString, Map<String, Object> param, File resultFile, String outputEncoding) throws IOException {
        ST result = new ST(stGroup == null ? this.createSTGroup() : stGroup, templateString);
        param.forEach(result::add);
        result.write(resultFile, ErrorManager.DEFAULT_ERROR_LISTENER, outputEncoding);
    }

    public String getAttributeName(String name) {
        String token = name;
        if (!Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
            token = this.getTemplateParameterAttribute() + name;
        }
        token = this.getTemplateVarStart() + token + this.getTemplateVarStop();
        return token;
    }

    @Override
    public String toString() {
        return "TemplateRender{" +
                "templateGroup=" + templateGroup +
                ", templateParameterAttribute='" + templateParameterAttribute + '\'' +
                ", templateVarStart=" + templateVarStart +
                ", templateVarStop=" + templateVarStop +
                ", encoding='" + encoding + '\'' +
                '}';
    }

    public static class Builder {

        private File templateGroup;

        private String templateParameterAttribute = "param";

        private char templateVarStart = '$';

        private char templateVarStop = '$';

        private String encoding = System.getProperty("file.encoding");

        public File getTemplateGroup() {
            return templateGroup;
        }

        public String getTemplateParameterAttribute() {
            return templateParameterAttribute;
        }

        public char getTemplateVarStart() {
            return templateVarStart;
        }

        public char getTemplateVarStop() {
            return templateVarStop;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public Builder setTemplateGroup(File templateGroup) {
            this.templateGroup = templateGroup;
            return this;
        }

        public Builder setTemplateParameterAttribute(String templateParameterAttribute) {
            this.templateParameterAttribute = templateParameterAttribute;
            return this;
        }

        public Builder setTemplateVarStart(char templateVarStart) {
            this.templateVarStart = templateVarStart;
            return this;
        }

        public Builder setTemplateVarStop(char templateVarStop) {
            this.templateVarStop = templateVarStop;
            return this;
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public TemplateRender build() {
            return new TemplateRender(this);
        }

    }
}
