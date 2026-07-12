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
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.table;
        }

        @Override
        public String defaultSettingsPath() {
            return "sql/ddlSettings.json";
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
            return ParameterUnit.table;
        }

        private static final int DATA_START_ROW = 1;

        // One output file per table (GenerateOption.resultPath()'s xlsxSchema branch appends
        // "<tableName>.json"), matching ddl/javaBean/fixedColumnDef rather than the old single
        // combined-schema file. unit=table's ComparableTableDto.resolve() hands write() a "dataSet" with
        // exactly one entry per call, so the loop below (and the shared template's own "dataSet.values"
        // iteration) needs no table-count-specific logic either way.
        // The shared xlsxSchemaTemplate.stg/.txt (also copied verbatim by Scaffold's xlsxSchema target, see
        // ScaffoldOption.writeSchemaTemplate) expect "dataSet" to hold, per table, a "one row per column"
        // shape: rows (COLUMN_NAME/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS per column) plus
        // dataset.PK/dataset.CELLS sub-tables. This keeps rowEntry(row)/cellEntry(row) identical whether
        // "dataSet" was built here (from real Column[]/primaryKeys[] metadata) or by Scaffold's unit=table +
        // unitSetting "separate" (from a user-edited dummy dataset).
        @Override
        @SuppressWarnings("unchecked")
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
            final Map<String, Object> schemaDataSet = new LinkedHashMap<>();
            dataSet.forEach((tableName, table) -> schemaDataSet.put(tableName, this.toSchemaTable(table)));
            super.write(option, resultFile, param.add("dataSet", schemaDataSet));
        }

        private Map<String, Object> toSchemaTable(final Map<String, Object> table) {
            final Column[] columns = (Column[]) table.get("columns");
            final Column[] primaryKeys = (Column[]) table.get("primaryKeys");
            final String tableName = table.get("tableName").toString();
            final List<Map<String, Object>> rows = IntStream.range(0, columns.length)
                    .mapToObj(i -> this.toColumnRow(columns[i].getColumnName(), tableName, i))
                    .toList();
            final Map<String, Map<String, Object>> columnRows = new LinkedHashMap<>();
            rows.forEach(row -> columnRows.put(row.get("COLUMN_NAME").toString(), row));
            final List<Map<String, Object>> pkRows = Arrays.stream(primaryKeys)
                    .map(pk -> columnRows.get(pk.getColumnName())).toList();
            final Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("PK", Map.of("rows", pkRows));
            dataset.put("CELLS", Map.of("rows", rows));
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("tableName", tableName);
            result.put("rows", rows);
            result.put("dataset", dataset);
            return result;
        }

        private Map<String, Object> toColumnRow(final String columnName, final String tableName, final int columnIndex) {
            final Map<String, Object> row = new LinkedHashMap<>();
            row.put("COLUMN_NAME", columnName);
            row.put("SHEET_NAME", tableName);
            row.put("DATA_START", String.valueOf(DATA_START_ROW));
            row.put("COLUMN_INDEX", String.valueOf(columnIndex));
            row.put("CELL_ADDRESS", new CellReference(DATA_START_ROW, columnIndex).formatAsString());
            return row;
        }
    }, javaBean("javabean/javaBeanTemplate.stg", "javabean/javaBeanTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
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

    protected ParameterUnit getFixedUnit() {
        return null;
    }

    protected void write(final GenerateOption option, final File resultFile, final Parameter param) throws IOException {
        option.templateOption().getTemplateRender()
              .write(this.getStGroup(), option.templateString(), param, resultFile, option.outputEncoding());
    }

}
