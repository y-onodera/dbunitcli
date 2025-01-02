package yo.dbunitcli.resource.poi;

import org.jxls.builder.JxlsOutputFile;
import org.jxls.builder.JxlsStreaming;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JxlsTemplateRender {

    private final String templateParameterAttribute;

    private final boolean formulaProcess;

    public static Builder builder() {
        return new Builder();
    }

    public JxlsTemplateRender(final Builder builder) {
        this.templateParameterAttribute = builder.getTemplateParameterAttribute();
        this.formulaProcess = builder.isFormulaProcess();
    }

    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    public void render(final File aTemplate, final File aResultFile, final Map<String, Object> param) throws IOException {
        final Map<String, Object> context = new HashMap<>();
        if (Optional.ofNullable(this.getTemplateParameterAttribute()).orElse("").isEmpty()) {
            context.putAll(param);
        } else {
            context.put(this.getTemplateParameterAttribute(), param);
        }
        try (final InputStream is = new FileInputStream(aTemplate)) {
            if (!aResultFile.getName().endsWith(".xls") && !this.formulaProcess) {
                JxlsPoiTemplateFillerBuilder.newInstance()
                        .withStreaming(JxlsStreaming.AUTO_DETECT)
                        .withTemplate(is)
                        .withUpdateCellDataArea(false)
                        .buildAndFill(context, new JxlsOutputFile(aResultFile));
            } else {
                JxlsPoiTemplateFillerBuilder.newInstance()
                        .withTemplate(is)
                        .buildAndFill(context, new JxlsOutputFile(aResultFile));
            }
        }
    }

    public static class Builder {

        private String templateParameterAttribute = "param";

        private boolean formulaProcess;

        public String getTemplateParameterAttribute() {
            return this.templateParameterAttribute;
        }

        public boolean isFormulaProcess() {
            return this.formulaProcess;
        }

        public Builder setTemplateParameterAttribute(final String templateParameterAttribute) {
            this.templateParameterAttribute = templateParameterAttribute;
            return this;
        }

        public Builder setFormulaProcess(final boolean formulaProcess) {
            this.formulaProcess = formulaProcess;
            return this;
        }

        public JxlsTemplateRender build() {
            return new JxlsTemplateRender(this);
        }

    }
}
