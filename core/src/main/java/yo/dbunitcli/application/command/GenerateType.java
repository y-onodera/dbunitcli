package yo.dbunitcli.application.command;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum GenerateType {
    txt(null, null) {
        @Override
        public String getTemplateString(final GenerateOption option) {
            final File templatePath = option.getTemplatePath();
            if (templatePath == null || !templatePath.exists() || !templatePath.isFile()) {
                throw new AssertionError(option.template() + " is not exist file",
                                         new IllegalArgumentException(String.valueOf(option.template())));
            }
            return FileResources.read(templatePath, option.templateOption().encoding());
        }
    }, xlsx(null, null) {
        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            JxlsTemplateRender.builder()
                              .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                              .setFormulaProcess(option.templateOption().formulaProcess())
                              .setEvaluateFormulas(option.templateOption().evaluateFormulas())
                              .setForceFormulaRecalc(option.templateOption().forceFormulaRecalc())
                              .setFastFormulaProcess(option.templateOption().fastFormulaProcess())
                              .setDeleteBlankCells(option.templateOption().deleteBlankCells()).build()
                              .render(option.getTemplatePath(), resultFile, param,
                                      option.unit() != ParameterUnit.record);
        }
    }, xls(null, null) {
        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            JxlsTemplateRender.builder()
                              .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                              .setFormulaProcess(option.templateOption().formulaProcess())
                              .setEvaluateFormulas(option.templateOption().evaluateFormulas())
                              .setForceFormulaRecalc(option.templateOption().forceFormulaRecalc())
                              .setFastFormulaProcess(option.templateOption().fastFormulaProcess())
                              .setDeleteBlankCells(option.templateOption().deleteBlankCells()).build()
                              .render(option.getTemplatePath(), resultFile, param,
                                      option.unit() != ParameterUnit.record);
        }
    }, settings("settings/settingTemplate.stg", "settings/settingTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }
    }, sql("sql/sqlTemplate.stg", null) {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.table;
        }

        @Override
        public String getTemplateString(final GenerateOption option) {
            return FileResources.readClasspathResource(switch (option.operation()) {
                case INSERT -> "sql/insertTemplate.txt";
                case DELETE -> "sql/deleteTemplate.txt";
                case UPDATE -> "sql/updateTemplate.txt";
                case CLEAN_INSERT -> "sql/cleanInsertTemplate.txt";
                default -> "sql/deleteInsertTemplate.txt";
            });
        }
    }, ddl("sql/ddlTemplate.stg", "sql/ddlTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public boolean supportsUserTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.table;
        }

        @Override
        public String defaultSettingsPath() {
            return "sql/ddlSettings.json";
        }

        @Override
        public String getTemplateString(final GenerateOption option) {
            if (Strings.isNotEmpty(option.template())) {
                return txt.getTemplateString(option);
            }
            return FileResources.readClasspathResource(this.getTemplatePath());
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final STGroup stGroup = Strings.isEmpty(option.templateOption().templateGroup())
                    ? this.getStGroup() : null;
            option.templateOption().getTemplateRender()
                  .write(stGroup, option.templateString(), param, resultFile, option.outputEncoding());
        }
    }, xlsxTemplate(null, null) {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            JxlsTemplateGenerator.createTemplate(resultFile, param);
        }
    }, xlsxSchema("xlsxschema/xlsxSchemaTemplate.stg", "xlsxschema/xlsxSchemaTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }

        private static final int DATA_START_ROW = 1;

        @Override
        @SuppressWarnings("unchecked")
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
            final List<Map<String, Object>> schemaRows = new ArrayList<>();
            final List<Map<String, Object>> schemaCells = new ArrayList<>();
            dataSet.values().forEach(table -> {
                final List<String> header = Arrays.stream((Column[]) table.get("columns"))
                        .map(Column::getColumnName).toList();
                schemaRows.add(this.toSchemaRow(table, header));
                schemaCells.add(this.toSchemaCell(table, header));
            });
            super.write(option, resultFile, param.add("schemaRows", schemaRows).add("schemaCells", schemaCells));
        }

        private Map<String, Object> toSchemaRow(final Map<String, Object> table, final List<String> header) {
            final Column[] primaryKeys = (Column[]) table.get("primaryKeys");
            final String tableName = table.get("tableName").toString();
            final List<String> breakKey = primaryKeys.length > 0
                    ? Arrays.stream(primaryKeys).map(Column::getColumnName).toList()
                    : header.isEmpty() ? List.of() : List.of(header.getFirst());
            final Map<String, Object> row = new LinkedHashMap<>();
            row.put("sheetName", tableName);
            row.put("tableName", tableName);
            row.put("header", header);
            row.put("dataStart", DATA_START_ROW);
            row.put("breakKey", breakKey);
            return row;
        }

        private Map<String, Object> toSchemaCell(final Map<String, Object> table, final List<String> header) {
            final String tableName = table.get("tableName").toString();
            final List<String> cellAddress = IntStream.range(0, header.size())
                    .mapToObj(col -> new CellReference(DATA_START_ROW, col).formatAsString())
                    .toList();
            final Map<String, Object> cell = new LinkedHashMap<>();
            cell.put("sheetName", tableName);
            cell.put("tableName", tableName);
            cell.put("header", header);
            cell.put("rows", List.of(cellAddress));
            return cell;
        }
    }, javaBean("javabean/javaBeanTemplate.stg", "javabean/javaBeanTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public boolean supportsUserTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.table;
        }

        @Override
        public String defaultSettingsPath() {
            return "javabean/javaBeanSettings.json";
        }

        @Override
        public String getTemplateString(final GenerateOption option) {
            if (Strings.isNotEmpty(option.template())) {
                return txt.getTemplateString(option);
            }
            return FileResources.readClasspathResource(this.getTemplatePath());
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final STGroup stGroup = Strings.isEmpty(option.templateOption().templateGroup())
                    ? this.getStGroup() : null;
            option.templateOption().getTemplateRender()
                  .write(stGroup, option.templateString(), param, resultFile, option.outputEncoding());
        }
    }, fixedColumnDef("fixedcolumndef/fixedColumnDefTemplate.stg", "fixedcolumndef/fixedColumnDefTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.table;
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final Column[] columns = (Column[]) param.get("columns");
            final String[] lengths =
                    Strings.isNotEmpty(option.fixedLength()) ? option.fixedLength().split(",") : new String[0];
            final List<FixedColumnDef> defs = IntStream.range(0, columns.length).mapToObj(
                    i -> new FixedColumnDef(columns[i].getColumnName(),
                                            i < lengths.length ? Integer.parseInt(lengths[i].trim()) :
                                                    option.defaultLength(), option.align(), " ")).toList();
            super.write(option, resultFile, param.add("columns", defs));
        }
    };

    private final String stgPath;
    private final String templatePath;

    GenerateType(final String stgPath, final String templatePath) {
        this.stgPath = stgPath;
        this.templatePath = templatePath;
    }

    private static STGroup loadStGroup(final String stgPath) {
        return new TemplateRender.Builder().setTemplateParameterAttribute(null).build().createSTGroup(stgPath);
    }

    public String getStgPath() {
        return this.stgPath;
    }

    public String getTemplatePath() {
        return this.templatePath;
    }

    public STGroup getStGroup() {
        return this.stgPath != null ? loadStGroup(this.stgPath) : null;
    }

    public String getTemplateString(final GenerateOption option) {
        return this.templatePath != null ? FileResources.readClasspathResource(this.templatePath) : null;
    }

    public String defaultSettingsPath() {
        return null;
    }

    public boolean isExcel() {
        return Stream.of(xlsx, xls)
                     .anyMatch(it -> it == this);
    }

    public boolean isText() {
        return Stream.of(xlsx, xls, xlsxTemplate)
                     .noneMatch(it -> it == this);
    }

    protected boolean isFixedTemplate() {
        return false;
    }

    public boolean supportsUserTemplate() {
        return false;
    }

    protected ParameterUnit getFixedUnit() {
        return null;
    }

    protected void write(final GenerateOption option, final File resultFile, final Parameter param) throws IOException {
        option.templateOption().getTemplateRender()
              .write(this.getStGroup(), option.templateString(), param, resultFile, option.outputEncoding());
    }

}
