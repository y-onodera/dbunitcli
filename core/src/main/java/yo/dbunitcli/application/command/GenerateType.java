package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.converter.FixedColumnDef;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
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
                throw new AssertionError(option.template() + " is not exist file"
                        , new IllegalArgumentException(String.valueOf(option.template())));
            }
            return FileResources.read(templatePath, option.templateOption().encoding());
        }
    },
    xlsx(null, null) {
        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param) throws IOException {
            JxlsTemplateRender.builder()
                              .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                              .setFormulaProcess(option.templateOption().formulaProcess())
                              .setEvaluateFormulas(option.templateOption().evaluateFormulas())
                              .setForceFormulaRecalc(option.templateOption().forceFormulaRecalc())
                              .setFastFormulaProcess(option.templateOption().fastFormulaProcess())
                              .setDeleteBlankCells(option.templateOption().deleteBlankCells())
                              .build()
                              .render(option.getTemplatePath(), resultFile, param, option.unit() != ParameterUnit.record);
        }
    },
    xls(null, null) {
        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            JxlsTemplateRender.builder()
                              .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                              .setFormulaProcess(option.templateOption().formulaProcess())
                              .setEvaluateFormulas(option.templateOption().evaluateFormulas())
                              .setForceFormulaRecalc(option.templateOption().forceFormulaRecalc())
                              .setFastFormulaProcess(option.templateOption().fastFormulaProcess())
                              .setDeleteBlankCells(option.templateOption().deleteBlankCells())
                              .build()
                              .render(option.getTemplatePath(), resultFile, param, option.unit() != ParameterUnit.record);
        }
    },
    settings("settings/settingTemplate.stg", "settings/settingTemplate.txt") {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }
    },
    sql("sql/sqlTemplate.stg", null) {
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
    },
    ddl("sql/ddlTemplate.stg", "sql/ddlTemplate.txt") {
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

        @Override
        @SuppressWarnings("unchecked")
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final List<Map<String, Object>> rows = (List<Map<String, Object>>) param.get("rows");
            final List<Map<String, Object>> pkRows = rows == null ? List.of()
                    : rows.stream().filter(row -> Boolean.TRUE.equals(row.get("IS_PK"))).toList();
            final List<String> pkColumnNames = pkRows.stream()
                    .map(row -> row.get("COLUMN_NAME").toString())
                    .toList();
            final String pkConstraintName = pkRows.isEmpty() ? null
                    : (String) pkRows.getFirst().get("PK_NAME");
            final String tableRemarks = rows == null || rows.isEmpty() ? ""
                    : (String) rows.getFirst().getOrDefault("TABLE_REMARKS", "");
            super.write(option, resultFile, param
                    .add("pkColumnNames", pkColumnNames)
                    .add("pkConstraintName", pkConstraintName)
                    .add("tableRemarks", tableRemarks));
        }
    },
    xlsxTemplate(null, null) {
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
    },
    javaBean("javabean/javaBeanTemplate.stg", "javabean/javaBeanTemplate.txt") {
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

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final String tableName = param.get("tableName").toString();
            super.write(option, resultFile, param.add("className", Strings.capitalize(tableName.toLowerCase())));
        }
    },
    fixedColumnDef("fixedcolumndef/fixedColumnDefTemplate.stg", "fixedcolumndef/fixedColumnDefTemplate.txt") {
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
            final String[] lengths = Strings.isNotEmpty(option.fixedLength())
                    ? option.fixedLength().split(",")
                    : new String[0];
            final List<FixedColumnDef> defs = IntStream.range(0, columns.length)
                                                       .mapToObj(i -> new FixedColumnDef(
                                                               columns[i].getColumnName(),
                                                               i < lengths.length ?
                                                                       Integer.parseInt(lengths[i].trim()) :
                                                                       option.defaultLength(),
                                                               option.align(),
                                                               " "))
                                                       .toList();
            super.write(option, resultFile, param.add("columns", defs));
        }
    };

    private final STGroup stGroup;
    private final String fixedTemplateString;

    GenerateType(final String stgPath, final String templatePath) {
        this.stGroup = stgPath != null ? loadStGroup(stgPath) : null;
        this.fixedTemplateString = templatePath != null ? FileResources.readClasspathResource(templatePath) : null;
    }

    private static STGroup loadStGroup(final String stgPath) {
        return new TemplateRender.Builder()
                .setTemplateParameterAttribute(null)
                .build()
                .createSTGroup(stgPath);
    }

    public boolean isAny(final GenerateType... expects) {
        return Stream.of(expects).anyMatch(it -> it == this);
    }

    public STGroup getStGroup() {
        return this.stGroup;
    }

    protected void write(final GenerateOption option, final File resultFile, final Parameter param) throws IOException {
        option.templateOption().getTemplateRender().write(this.getStGroup()
                , option.templateString()
                , param
                , resultFile
                , option.outputEncoding());
    }

    protected boolean isFixedTemplate() {
        return false;
    }

    protected ParameterUnit getFixedUnit() {
        return null;
    }

    public String getTemplateString(final GenerateOption option) {
        return this.fixedTemplateString;
    }

    public boolean isExcel() {
        return this.isAny(xlsx, xls);
    }

    public boolean isMetadataOnly() {
        return this.isAny(settings);
    }

    public boolean requiresJdbcMetaData() {
        return this.isAny(javaBean);
    }

    public String defaultSettingsPath() {
        return null;
    }

    public boolean isText() {
        return !this.isAny(xlsx, xls, xlsxTemplate);
    }
}
