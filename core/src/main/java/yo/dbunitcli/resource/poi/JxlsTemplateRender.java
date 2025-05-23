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

    private final boolean evaluateFormulas;

    private final boolean forceFormulaRecalc;

    public static Builder builder() {
        return new Builder();
    }

    public JxlsTemplateRender(final Builder builder) {
        this.templateParameterAttribute = builder.getTemplateParameterAttribute();
        this.formulaProcess = builder.isFormulaProcess();
        this.evaluateFormulas = builder.isEvaluateFormulas();
        this.forceFormulaRecalc = builder().isForceFormulaRecalc();
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
                        .withStreaming(JxlsStreaming.STREAMING_ON)
                        .withTemplate(is)
                        .withUpdateCellDataArea(false)
                        .buildAndFill(context, new JxlsOutputFile(aResultFile));
            } else {
                JxlsPoiTemplateFillerBuilder.newInstance()
                        .withTemplate(is)
                        .withRecalculateFormulasBeforeSaving(this.evaluateFormulas)
                        .withRecalculateFormulasOnOpening(this.forceFormulaRecalc)
                        .buildAndFill(context, new JxlsOutputFile(aResultFile));
            }
        }
    }

    public static class Builder {

        private String templateParameterAttribute = "param";

        private boolean formulaProcess;

        private boolean evaluateFormulas = true;
        private boolean forceFormulaRecalc = false;

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

        public Builder setEvaluateFormulas(final boolean evaluateFormulas) {
            this.evaluateFormulas = evaluateFormulas;
            return this;
        }

        public Builder setForceFormulaRecalc(final boolean forceFormulaRecalc) {
            this.forceFormulaRecalc = forceFormulaRecalc;
            return this;
        }

        public boolean isEvaluateFormulas() {
            return this.evaluateFormulas;
        }

        public JxlsTemplateRender build() {
            return new JxlsTemplateRender(this);
        }

        public boolean isForceFormulaRecalc() {
            return this.forceFormulaRecalc;
        }
    }
}
