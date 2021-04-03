package yo.dbunitcli.resource.poi;

import com.google.common.base.Strings;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.*;
import java.util.Map;

public class JxlsTemplateRender {

    private String templateParameterAttribute;

    public JxlsTemplateRender(Builder builder) {
        this.templateParameterAttribute = builder.getTemplateParameterAttribute();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateParameterAttribute() {
        return templateParameterAttribute;
    }

    public void render(File aTemplate, File aResultFile, Map<String, Object> param) throws IOException {
        try (InputStream is = new FileInputStream(aTemplate)) {
            try (OutputStream os = new FileOutputStream(aResultFile)) {
                Context context = new Context();
                if (Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
                    param.forEach(context::putVar);
                } else {
                    context.putVar(this.getTemplateParameterAttribute(), param);
                }
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
    }

    public static class Builder {

        private String templateParameterAttribute = "param";

        public String getTemplateParameterAttribute() {
            return templateParameterAttribute;
        }

        public Builder setTemplateParameterAttribute(String templateParameterAttribute) {
            this.templateParameterAttribute = templateParameterAttribute;
            return this;
        }

        public JxlsTemplateRender build() {
            return new JxlsTemplateRender(this);
        }

    }
}
