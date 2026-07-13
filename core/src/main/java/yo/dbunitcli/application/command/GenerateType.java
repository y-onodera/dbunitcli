package yo.dbunitcli.application.command;

import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableFixedColumnDefMetaDataProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxSchemaMetaDataProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        @Override
        protected boolean loadData() {
            return false;
        }

        @Override
        protected boolean useJdbcMetaData() {
            return true;
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

        @Override
        protected boolean useJdbcMetaData() {
            return true;
        }

        @Override
        protected String resultPathTemplate(final GenerateOption option) {
            return GenerateType.sqlResultPath(option);
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

        @Override
        protected boolean useJdbcMetaData() {
            return true;
        }

        @Override
        protected String resultPathTemplate(final GenerateOption option) {
            return GenerateType.sqlResultPath(option);
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
        protected boolean loadData() {
            return false;
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

        @Override
        protected boolean loadData() {
            return false;
        }

        @Override
        protected String resultPathTemplate(final GenerateOption option) {
            return GenerateType.tableFileResultPath(option, ".json");
        }

        @Override
        protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
            return new ComparableXlsxSchemaMetaDataProducer(producer);
        }

        // One output file per table (see resultPathTemplate() above), matching ddl/javaBean/fixedColumnDef
        // rather than the old single combined-schema file. unit=table's ComparableTableDto.resolve() hands
        // write() a "dataSet" with exactly one entry per call, so the loop below (and the shared template's
        // own "dataSet.values" iteration) needs no table-count-specific logic either way.
        // The shared xlsxSchemaTemplate.stg/.txt (also copied verbatim by Scaffold's xlsxSchema target, see
        // ScaffoldOption.writeSchemaTemplate) expect "dataSet" to hold, per table, a "one row per column"
        // shape: rows (COLUMN_NAME/SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS per column) plus
        // dataset.PK/dataset.CELLS sub-tables. wrapProducer() above already produces "rows" in that shape
        // from real table metadata (ComparableXlsxSchemaMetaDataProducer); here we only split PK vs all.
        @Override
        @SuppressWarnings("unchecked")
        protected void write(final GenerateOption option, final File resultFile, final Parameter param)
                throws IOException {
            final Map<String, Map<String, Object>> dataSet = (Map<String, Map<String, Object>>) param.get("dataSet");
            final Map<String, Object> schemaDataSet = new LinkedHashMap<>();
            dataSet.forEach((tableName, table) -> schemaDataSet.put(tableName,
                    this.toSchemaTable(tableName, (List<Map<String, Object>>) table.get("rows"))));
            super.write(option, resultFile, param.add("dataSet", schemaDataSet));
        }

        private Map<String, Object> toSchemaTable(final String tableName, final List<Map<String, Object>> rows) {
            final List<Map<String, Object>> pkRows = rows.stream()
                    .filter(row -> Boolean.TRUE.equals(row.get("IS_PK")))
                    .toList();
            final Map<String, Object> dataset = new LinkedHashMap<>();
            dataset.put("PK", Map.of("rows", pkRows));
            dataset.put("CELLS", Map.of("rows", rows));
            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("tableName", tableName);
            result.put("rows", rows);
            result.put("dataset", dataset);
            return result;
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

        @Override
        protected boolean useJdbcMetaData() {
            return true;
        }

        @Override
        protected String resultPathTemplate(final GenerateOption option) {
            return GenerateType.tableFileResultPath(option, "", "", ".java", "snakeToUpperCamel");
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
        protected boolean loadData() {
            return false;
        }

        @Override
        protected String resultPathTemplate(final GenerateOption option) {
            return GenerateType.tableFileResultPath(option, ".json");
        }

        @Override
        protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
            final String[] lengths =
                    Strings.isNotEmpty(option.fixedLength()) ? option.fixedLength().split(",") : new String[0];
            return new ComparableFixedColumnDefMetaDataProducer(producer, lengths, option.defaultLength(), option.align());
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

    protected boolean loadData() {
        return true;
    }

    protected boolean useJdbcMetaData() {
        return false;
    }

    // ParameterUnit.table.templateStream()のみがこの戻り値を消費する(producer.lazyLoad(true)経由)。
    // unit=record/datasetはdataSetToStream(loader, producer.param())にフォールバックするため、
    // ここをoverrideしてもunit=table以外では反映されない。
    protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
        return producer;
    }

    protected String resultPathTemplate(final GenerateOption option) {
        return option.resultPath();
    }

    private static String tableFileResultPath(final GenerateOption option, final String extension) {
        return GenerateType.tableFileResultPath(option, "", "", extension);
    }

    private static String tableFileResultPath(final GenerateOption option, final String prefix, final String suffix,
                                              final String extension) {
        return GenerateType.tableFileResultPath(option, prefix, suffix, extension, null);
    }

    private static String tableFileResultPath(final GenerateOption option, final String prefix, final String suffix,
                                              final String extension, final String nameFormat) {
        final String tableName = nameFormat == null
                ? option.templateOption().getTemplateRender().getAttributeName("tableName")
                : option.templateOption().getTemplateRender().getAttributeName("tableName", nameFormat);
        return option.resultPath() + "/" + prefix + tableName + suffix + extension;
    }

    private static String sqlResultPath(final GenerateOption option) {
        return GenerateType.tableFileResultPath(option, option.sqlFilePrefix(), option.sqlFileSuffix(), ".sql");
    }

    protected void write(final GenerateOption option, final File resultFile, final Parameter param) throws IOException {
        option.templateOption().getTemplateRender()
              .write(this.getStGroup(), option.templateString(), param, resultFile, option.outputEncoding());
    }

}
