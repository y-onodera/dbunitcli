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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public enum GenerateType {
    txt {
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
    xlsx {
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
    xls {
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
    settings {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }

        @Override
        public String getTemplateString(final GenerateOption option) {
            return FileResources.readClasspathResource("settings/settingTemplate.txt");
        }

        @Override
        protected STGroup getStGroup() {
            return new TemplateRender.Builder()
                    .setTemplateParameterAttribute(null)
                    .build()
                    .createSTGroup("settings/settingTemplate.stg");
        }
    },
    sql {
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
            return FileResources.readClasspathResource(this.getSqlTemplate(option.operation()));
        }

        @Override
        protected STGroup getStGroup() {
            return new TemplateRender.Builder()
                    .setTemplateParameterAttribute(null)
                    .build()
                    .createSTGroup("sql/sqlTemplate.stg");
        }

        private String getSqlTemplate(DbOperation operation) {
            return switch (operation) {
                case INSERT -> "sql/insertTemplate.txt";
                case DELETE -> "sql/deleteTemplate.txt";
                case UPDATE -> "sql/updateTemplate.txt";
                case CLEAN_INSERT -> "sql/cleanInsertTemplate.txt";
                default -> "sql/deleteInsertTemplate.txt";
            };
        }

    },
    ddl {
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
            return FileResources.readClasspathResource("sql/ddlTemplate.txt");
        }

        @Override
        protected STGroup getStGroup() {
            return sql.getStGroup();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final List<Map<String, Object>> rows = (List<Map<String, Object>>) param.get("rows");
            final List<String> pkColumnNames = rows == null ? List.of()
                    : rows.stream()
                          .filter(row -> Boolean.TRUE.equals(row.get("IS_PK")))
                          .map(row -> row.get("COLUMN_NAME").toString())
                          .toList();
            final String tableRemarks = rows == null || rows.isEmpty() ? ""
                    : (String) rows.getFirst().getOrDefault("TABLE_REMARKS", "");
            super.write(option, resultFile, param
                    .add("pkColumnNames", pkColumnNames)
                    .add("tableRemarks", tableRemarks));
        }
    },
    xlsxTemplate {
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
    javaBean {
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
            return FileResources.readClasspathResource("javabean/javaBeanTemplate.txt");
        }

        @Override
        protected STGroup getStGroup() {
            return new TemplateRender.Builder()
                    .setTemplateParameterAttribute(null)
                    .build()
                    .createSTGroup("javabean/javaBeanTemplate.stg");
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final String tableName = param.get("tableName").toString();
            super.write(option, resultFile, param.add("className", Strings.capitalize(tableName.toLowerCase())));
        }
    },
    scaffold {
        @Override
        public boolean isFixedTemplate() {
            return true;
        }

        @Override
        public ParameterUnit getFixedUnit() {
            return ParameterUnit.dataset;
        }

        @Override
        public String getTemplateString(final GenerateOption option) {
            return "";
        }

        @Override
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final File baseDir = option.getResultDir();
            final File settingDir = new File(baseDir, "resources/setting");
            final File templateDir = new File(baseDir, "resources/template");
            settingDir.mkdirs();
            templateDir.mkdirs();
            this.writeClasspathResource("scaffold/scaffoldSettings.json", new File(settingDir, "scaffold.json"));
            this.writeClasspathResource("sql/ddlTemplate.stg", new File(templateDir, "ddl.stg"));
            this.writeClasspathResource("sql/ddlTemplate.txt", new File(templateDir, "ddl.txt"));
            this.writeClasspathResource("javabean/javaBeanTemplate.stg", new File(templateDir, "javaBean.stg"));
            this.writeClasspathResource("javabean/javaBeanTemplate.txt", new File(templateDir, "javaBean.txt"));
        }

        private void writeClasspathResource(final String resource, final File dest) throws IOException {
            final String content = FileResources.readClasspathResource(resource);
            Files.writeString(dest.toPath(), content, StandardCharsets.UTF_8);
        }
    },
    fixedColumnDef {
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
            return FileResources.readClasspathResource("fixedcolumndef/fixedColumnDefTemplate.txt");
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
            super.write(option,resultFile,param.add("columns", defs));
        }

        @Override
        protected STGroup getStGroup() {
            return new TemplateRender.Builder()
                    .setTemplateParameterAttribute(null)
                    .build()
                    .createSTGroup("fixedcolumndef/fixedColumnDefTemplate.stg");
        }
    };

    public boolean isAny(final GenerateType... expects) {
        return Stream.of(expects).anyMatch(it -> it == this);
    }

    protected STGroup getStGroup() {
        return null;
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

    protected String getTemplateString(final GenerateOption option) {
        return null;
    }

    public boolean isExcel() {
        return this.isAny(xlsx, xls);
    }

    public boolean isText() {
        return !this.isAny(xlsx, xls, xlsxTemplate);
    }
}
