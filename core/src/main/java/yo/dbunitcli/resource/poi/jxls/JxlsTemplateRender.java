package yo.dbunitcli.resource.poi.jxls;

import org.jxls.builder.JxlsOutputFile;
import org.jxls.builder.JxlsStreaming;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import yo.dbunitcli.common.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record JxlsTemplateRender(
        String templateParameterAttribute,
        boolean formulaProcess,
        boolean evaluateFormulas,
        boolean forceFormulaRecalc,
        boolean fastFormulaProcess
) {

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateParameterAttribute() {
        return this.templateParameterAttribute;
    }

    public void render(final File aTemplate, final File aResultFile, final Parameter param, final boolean useStreamingEach) throws IOException {
        final Map<String, Object> context = new HashMap<>();
        if (Optional.ofNullable(this.getTemplateParameterAttribute()).orElse("").isEmpty()) {
            param.forEach(context::put);
        } else {
            context.put(this.getTemplateParameterAttribute(), param);
        }
        try (final InputStream is = new FileInputStream(aTemplate)) {
            final XlsCommentAreaBuilder areaBuilder = new XlsCommentAreaBuilder();
            if (useStreamingEach) {
                areaBuilder.addCommandMapping(StreamingEachCommand.COMMAND_NAME, StreamingEachCommand.class);
            }
            final JxlsPoiTemplateFillerBuilder builder = JxlsPoiTemplateFillerBuilder.newInstance()
                    .withAreaBuilder(areaBuilder)
                    .withTemplate(is)
                    .withTransformerFactory(new UserFormulasValueClearPoiTransformerFactory());
            if (this.fastFormulaProcess) {
                builder.withFastFormulaProcessor();
            }

            if (!aResultFile.getName().endsWith(".xls") && !this.formulaProcess) {
                builder
                        .withStreaming(JxlsStreaming.STREAMING_ON)
                        .withUpdateCellDataArea(false)
                        .buildAndFill(context, new JxlsOutputFile(aResultFile));
            } else {
                builder
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
        private boolean fastFormulaProcess = false;

        public String getTemplateParameterAttribute() {
            return this.templateParameterAttribute;
        }

        public boolean isFormulaProcess() {
            return this.formulaProcess;
        }

        public boolean isEvaluateFormulas() {
            return this.evaluateFormulas;
        }

        public boolean isForceFormulaRecalc() {
            return this.forceFormulaRecalc;
        }

        public boolean isFastFormulaProcess() {
            return this.fastFormulaProcess;
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

        public Builder setFastFormulaProcess(final boolean fastFormulaProcess) {
            this.fastFormulaProcess = fastFormulaProcess;
            return this;
        }

        public JxlsTemplateRender build() {
            return new JxlsTemplateRender(
                    this.getTemplateParameterAttribute(),
                    this.isFormulaProcess(),
                    this.isEvaluateFormulas(),
                    this.isForceFormulaRecalc(),
                    this.isFastFormulaProcess()
            );
        }
    }
}