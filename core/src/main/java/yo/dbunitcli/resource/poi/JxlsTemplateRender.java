package yo.dbunitcli.resource.poi;

import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.SelectSheetsForStreamingPoiTransformer;
import org.jxls.util.JxlsHelper;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JxlsTemplateRender {

    private final String templateParameterAttribute;

    private final boolean formulaProcess;

    public JxlsTemplateRender(Builder builder) {
        this.templateParameterAttribute = builder.getTemplateParameterAttribute();
        this.formulaProcess = builder.isFormulaProcess();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateParameterAttribute() {
        return templateParameterAttribute;
    }

    public void render(File aTemplate, File aResultFile, Map<String, Object> param) throws IOException {
        try (InputStream is = new FileInputStream(aTemplate)) {
            Context context = new Context();
            if (Strings.isNullOrEmpty(this.getTemplateParameterAttribute())) {
                param.forEach(context::putVar);
            } else {
                context.putVar(this.getTemplateParameterAttribute(), param);
            }
            try (OutputStream os = new FileOutputStream(aResultFile)) {
                if (!aResultFile.getName().endsWith(".xls") && !this.formulaProcess) {
                    this.streamingRender(is, context, os);
                } else {
                    JxlsHelper.getInstance()
                            .setEvaluateFormulas(true)
                            .processTemplate(is, os, context);
                }
            }
        }
    }

    protected void streamingRender(InputStream is, Context context, OutputStream os) throws IOException {
        Workbook workbook = WorkbookFactory.create(is);
        SelectSheetsForStreamingPoiTransformer transformer = new SelectSheetsForStreamingPoiTransformer(workbook);
        Set<String> streamingSheets = new HashSet<>();
        for (int i = 0, j = workbook.getNumberOfSheets(); i < j; i++) {
            streamingSheets.add(workbook.getSheetAt(i).getSheetName());
        }
        transformer.setDataSheetsToUseStreaming(streamingSheets);
        AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
        List<Area> xlsAreaList = areaBuilder.build();
        for (Area xlsArea : xlsAreaList) {
            xlsArea.applyAt(new CellRef(xlsArea.getStartCellRef().getCellName()), context);
        }
        transformer.getWorkbook().write(os);
        if (transformer.getWorkbook() instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) transformer.getWorkbook()).dispose();
        }
    }

    public static class Builder {

        private String templateParameterAttribute = "param";

        private boolean formulaProcess;

        public String getTemplateParameterAttribute() {
            return templateParameterAttribute;
        }

        public boolean isFormulaProcess() {
            return this.formulaProcess;
        }

        public Builder setTemplateParameterAttribute(String templateParameterAttribute) {
            this.templateParameterAttribute = templateParameterAttribute;
            return this;
        }

        public Builder setFormulaProcess(boolean formulaProcess) {
            this.formulaProcess = formulaProcess;
            return this;
        }

        public JxlsTemplateRender build() {
            return new JxlsTemplateRender(this);
        }

    }
}
