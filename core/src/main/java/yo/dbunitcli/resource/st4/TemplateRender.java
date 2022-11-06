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

    public TemplateRender(final Builder builder) {
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
        return this.templateParameterAttribute;
    }

    public char getTemplateVarStart() {
        return this.templateVarStart;
    }

    public char getTemplateVarStop() {
        return this.templateVarStop;
    }

    public String render(final File aFile, final Map<String, Object> parameter) {
        return this.render(Files.read(aFile, this.getEncoding()), parameter);
    }

    public String render(final String target, final Map<String, Object> parameter) {
        if (parameter.size() > 0) {
            return this.createST(target, parameter).render();
        }
        return target;
    }

    public String replaceParameter(final String target, final Map<String, Object> parameter) {
        String result = target;
        for (final Map.Entry<String, Object> entry : parameter.entrySet()) {
            result = result.replace(this.getAttributeName(entry.getKey()), entry.getValue().toString());
        }
        return result;
    }

    public ST createST(final String result) {
        return new ST(this.createSTGroup(), result);
    }

    public ST createST(final String target, final Map<String, Object> parameter) {
        final String template = this.replaceParameter(target, parameter);
        final ST st = this.createST(template);
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

    public STGroup createSTGroup(final File groupFile) {
        if (groupFile == null) {
            return this.createSTGroup("");
        }
        return this.createSTGroup(groupFile.getAbsolutePath());
    }

    public STGroup createSTGroup(final String fileName) {
        final STGroup stGroup;
        if (Strings.isNullOrEmpty(fileName)) {
            stGroup = new STGroup(this.templateVarStart, this.templateVarStop);
        } else {
            stGroup = new STGroupFile(fileName, this.templateVarStart, this.templateVarStop);
        }
        stGroup.registerRenderer(String.class, new SqlEscapeStringRenderer());
        return stGroup;
    }

    public void write(final String templateString, final Map<String, Object> param, final File resultFile, final String outputEncoding) throws IOException {
        this.write(this.createSTGroup(), templateString, param, resultFile, outputEncoding);
    }

    public void write(final STGroup stGroup, final String templateString, final Map<String, Object> param, final File resultFile, final String outputEncoding) throws IOException {
        final ST result = new ST(stGroup == null ? this.createSTGroup() : stGroup, templateString);
        param.forEach(result::add);
        result.write(resultFile, ErrorManager.DEFAULT_ERROR_LISTENER, outputEncoding);
    }

    public String getAttributeName(final String name) {
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
                "templateGroup=" + this.templateGroup +
                ", templateParameterAttribute='" + this.templateParameterAttribute + '\'' +
                ", templateVarStart=" + this.templateVarStart +
                ", templateVarStop=" + this.templateVarStop +
                ", encoding='" + this.encoding + '\'' +
                '}';
    }

    public static class Builder {

        private File templateGroup;

        private String templateParameterAttribute = "param";

        private char templateVarStart = '$';

        private char templateVarStop = '$';

        private String encoding = System.getProperty("file.encoding");

        public File getTemplateGroup() {
            return this.templateGroup;
        }

        public String getTemplateParameterAttribute() {
            return this.templateParameterAttribute;
        }

        public char getTemplateVarStart() {
            return this.templateVarStart;
        }

        public char getTemplateVarStop() {
            return this.templateVarStop;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public Builder setTemplateGroup(final File templateGroup) {
            this.templateGroup = templateGroup;
            return this;
        }

        public Builder setTemplateParameterAttribute(final String templateParameterAttribute) {
            this.templateParameterAttribute = templateParameterAttribute;
            return this;
        }

        public Builder setTemplateVarStart(final char templateVarStart) {
            this.templateVarStart = templateVarStart;
            return this;
        }

        public Builder setTemplateVarStop(final char templateVarStop) {
            this.templateVarStop = templateVarStop;
            return this;
        }

        public Builder setEncoding(final String encoding) {
            this.encoding = encoding;
            return this;
        }

        public TemplateRender build() {
            return new TemplateRender(this);
        }

    }
}
