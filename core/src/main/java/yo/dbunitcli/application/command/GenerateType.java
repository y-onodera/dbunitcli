package yo.dbunitcli.application.command;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDdlMetaDataProducer;
import yo.dbunitcli.dataset.producer.ComparableFixedColumnDefMetaDataProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxSchemaMetaDataProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        public Writer writer(final GenerateOption option) throws IOException {
            return GenerateType.excelWriter(option);
        }
    }, xls(null, null) {
        @Override
        public Writer writer(final GenerateOption option) throws IOException {
            return GenerateType.excelWriter(option);
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
        protected boolean loadData() {
            return false;
        }

        @Override
        protected boolean useJdbcMetaData() {
            return true;
        }

        @Override
        public boolean isParallelizable() {
            return false;
        }

        @Override
        protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
            return ComparableDdlMetaDataProducer.forSource(producer);
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
        public Writer writer(final GenerateOption option) {
            return JxlsTemplateGenerator::createTemplate;
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

        // separateルールでPK/CELLSサブテーブルを合成する（Scaffoldがコピーする雛型と同一ファイル。
        // テンプレートのsubTable.PK/subTable.CELLS参照はこの分割が前提）
        @Override
        public String defaultSettingsPath() {
            return "xlsxschema/xlsxSchemaSettings.json";
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
        public boolean isParallelizable() {
            return false;
        }

        @Override
        protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
            return new ComparableXlsxSchemaMetaDataProducer(producer);
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
        protected boolean loadData() {
            return false;
        }

        @Override
        protected boolean useJdbcMetaData() {
            return true;
        }

        @Override
        public boolean isParallelizable() {
            return false;
        }

        @Override
        protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
            return ComparableDdlMetaDataProducer.forSource(producer);
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
        public boolean isParallelizable() {
            return false;
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

    /**
     * 並列化に対応するか。srcのproducerを包む形式（wrapProducer）は、包んだproducerが並列読込に対応しないため不可
     */
    public boolean isParallelizable() {
        return true;
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

    /**
     * テンプレートの読込・コンパイルを1回だけ行い、出力処理を返す。戻り値は複数スレッドから同時に呼ばれうる
     */
    public Writer writer(final GenerateOption option) throws IOException {
        final TemplateRender render = option.templateOption().getTemplateRender();
        final ST template = render.compile(this.getStGroup(), option.templateString());
        return (resultFile, param) -> render.write(template, param, resultFile, option.outputEncoding());
    }

    private static Writer excelWriter(final GenerateOption option) throws IOException {
        final JxlsTemplateRender render = JxlsTemplateRender.builder()
                .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                .setFormulaProcess(option.templateOption().formulaProcess())
                .setEvaluateFormulas(option.templateOption().evaluateFormulas())
                .setForceFormulaRecalc(option.templateOption().forceFormulaRecalc())
                .setFastFormulaProcess(option.templateOption().fastFormulaProcess())
                .setDeleteBlankCells(option.templateOption().deleteBlankCells()).build();
        final byte[] template = Files.readAllBytes(option.getTemplatePath().toPath());
        final boolean useStreamingEach = option.unit() != ParameterUnit.record;
        return (resultFile, param) -> render.render(template, resultFile, param, useStreamingEach);
    }

    @FunctionalInterface
    public interface Writer {
        void write(File resultFile, Parameter param) throws IOException;
    }

}
