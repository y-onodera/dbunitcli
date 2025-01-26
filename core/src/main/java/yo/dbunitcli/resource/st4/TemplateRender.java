package yo.dbunitcli.resource.st4;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.ErrorManager;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public record TemplateRender(
        File templateGroup
        , String templateParameterAttribute
        , char templateVarStart
        , char templateVarStop
        , String encoding) {

    public static Builder builder() {
        return new Builder();
    }

    public TemplateRender() {
        this(new Builder());
    }

    public TemplateRender(final Builder builder) {
        this(builder.getTemplateGroup()
                , builder.getTemplateParameterAttribute()
                , builder.getTemplateVarStart()
                , builder.getTemplateVarStop()
                , builder.getEncoding()
        );
    }

    public String render(final File aFile, final Map<String, Object> parameter) {
        return this.render(FileResources.read(aFile, this.encoding()), parameter);
    }

    public String render(final String target, final Map<String, Object> parameter) {
        if (!parameter.isEmpty()) {
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
        if (Optional.ofNullable(this.templateParameterAttribute()).orElse("").isEmpty()) {
            parameter.forEach(st::add);
        } else {
            st.add(this.templateParameterAttribute(), parameter);
        }
        return st;
    }

    public STGroup createSTGroup() {
        return this.createSTGroup(this.templateGroup());
    }

    public STGroup createSTGroup(final File groupFile) {
        if (groupFile == null) {
            return this.createSTGroup("");
        }
        return this.createSTGroup(groupFile.getAbsolutePath());
    }

    public STGroup createSTGroup(final String fileName) {
        final STGroup stGroup;
        if (Optional.ofNullable(fileName).orElse("").isEmpty()) {
            stGroup = new STGroup(this.templateVarStart(), this.templateVarStop());
        } else {
            stGroup = new STGroupFile(fileName, this.templateVarStart(), this.templateVarStop());
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
        if (!Optional.ofNullable(this.templateParameterAttribute()).orElse("").isEmpty()) {
            token = this.templateParameterAttribute() + name;
        }
        token = this.templateVarStart() + token + this.templateVarStop();
        return token;
    }

    public static class Builder {

        private File templateGroup;

        private String templateParameterAttribute = "param";

        private char templateVarStart = '$';

        private char templateVarStop = '$';

        private String encoding = Charset.defaultCharset().displayName();

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
