package yo.dbunitcli.application.command;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

public record ScaffoldOption(
        Parameter parameter
        , String resultDir
        , String target
        , String settingName
        , String unitSettingName
        , String templateName
        , String parameterName
        , String commandType
        , String[] commandInput
        , DataSetLoadOption dataset
        , ResultType datasetType
        , String datasetEncoding
) implements CommandLineOption<ScaffoldDto> {

    private static final String COMMAND_INPUT_PREFIX = "-commandInput.";
    private static final String DATASET_SRC_DIR = "src";
    private static final Column[] DDL_SCHEMA_COLUMNS = {
            new Column("COLUMN_NAME", DataType.VARCHAR),
            new Column("TYPE_NAME", DataType.VARCHAR),
            new Column("COLUMN_SIZE", DataType.VARCHAR),
            new Column("DECIMAL_DIGITS", DataType.VARCHAR),
            new Column("NULLABLE", DataType.VARCHAR),
            new Column("IS_PK", DataType.VARCHAR),
            new Column("PK_NAME", DataType.VARCHAR),
            new Column("REMARKS", DataType.VARCHAR),
            new Column("TABLE_REMARKS", DataType.VARCHAR),
            new Column("TABLE_NAME", DataType.VARCHAR),
            new Column("PACKAGE", DataType.VARCHAR)
    };
    private static final String DDL_SCHEMA_HEADER_NAMES = ScaffoldOption.headerNames(DDL_SCHEMA_COLUMNS);
    // Unlike DDL_SCHEMA_COLUMNS (generic DDL-shaped dummy row, needed by ddl/javaBean's own settings
    // json to derive TYPE_NAME/COLUMN_SIZE/... columns), xlsxSchema/fixedColumnDef drive a custom
    // generateType=txt template with no Java-side precomputation, so writeDatasetSrcFiles instead
    // writes each target's dataset src rows directly in the shape its ScaffoldTemplate consumes,
    // keeping the unitSetting sample resource free of column-renaming/derivation busywork.
    // SHEET_NAME/DATA_START/COLUMN_INDEX/CELL_ADDRESS are seeded with the same values the built-in
    // -generateType=xlsxSchema would compute (sheet name == table name, dataStart 1, positional
    // index, POI CellReference(dataStart, columnIndex)), but unlike the built-in generateType
    // they're plain editable cells here: the fixed generateType=xlsxSchema can never produce a
    // sheetName that differs from tableName, a non-default dataStart, a non-contiguous columnIndex,
    // or a cellAddress pointing anywhere but the default grid position, since those are hardcoded
    // in GenerateType.xlsxSchema.write(); editing this dataset is the only way to get that.
    private static final int XLSX_SCHEMA_DEFAULT_DATA_START = 1;
    private static final Column[] XLSX_SCHEMA_DATASET_COLUMNS = {
            new Column("COLUMN_NAME", DataType.VARCHAR),
            new Column("IS_PK", DataType.VARCHAR),
            new Column("SHEET_NAME", DataType.VARCHAR),
            new Column("DATA_START", DataType.VARCHAR),
            new Column("COLUMN_INDEX", DataType.VARCHAR),
            new Column("CELL_ADDRESS", DataType.VARCHAR)
    };
    private static final Column[] FIXED_COLUMN_DEF_DATASET_COLUMNS = {
            new Column("name", DataType.VARCHAR),
            new Column("length", DataType.VARCHAR),
            new Column("align", DataType.VARCHAR),
            new Column("pad", DataType.VARCHAR)
    };
    // These are scaffold-only sample resources, deliberately NOT wired through
    // GenerateType.defaultSettingsPath(): that hook is a global default applied to every plain
    // -generateType=xlsxSchema/fixedColumnDef invocation (even outside scaffold), and the "separate into
    // named PK/CELLS sub-tables" rules below only make sense against the dummy src this scaffold writes,
    // not an arbitrary real dataset.
    private static final String XLSX_SCHEMA_SAMPLE_SETTINGS_PATH = "xlsxschema/xlsxSchemaSettings.json";
    private static final String FIXED_COLUMN_DEF_SAMPLE_SETTINGS_PATH = "fixedcolumndef/fixedColumnDefSettings.json";
    // fixedColumnDefTemplate.stg's columnEntry(col) macro is reused byte-for-byte (copied from the
    // classpath); only this driving .txt differs from the built-in one, iterating "rows" (the per-column
    // descriptor rows written by writeDatasetSrcFiles, already in name/length/align/pad shape) instead of
    // the Java-precomputed "columns" list, since generateType=txt has no Java-side precomputation step.
    // The unitSetting sample resource (fixedcolumndef/fixedColumnDefSettings.json) has nothing left to
    // derive and is kept as an empty placeholder for callers who want to add filtering/renaming later.
    private static final String FIXED_COLUMN_DEF_SCAFFOLD_TXT_PATH =
            "fixedcolumndef/fixedColumnDefScaffoldTemplate.txt";
    // Mirrors xlsxSchemaTemplate.stg's rowEntry(row)/cellEntry(cell) JSON shape (both "rows" and "cells"
    // sections), reading straight off the "rows"/"dataset.PK.rows"/"dataset.CELLS.rows" attributes that
    // unit=table + the unitSetting sample resource (xlsxschema/xlsxSchemaSettings.json) already provide,
    // since generateType=txt has no Java-side precomputation. IS_PK is the one column that genuinely
    // needs the unitSetting's PK split; the CELLS split is a plain identity rename (no filter) so the
    // "cells" section can be customized (filtered/reordered) independently of "rows" later without
    // touching this template. Every JSON field (sheetName/dataStart/columnIndex/cellAddress/header/
    // breakKey) reads straight off a same-named dataset column.
    private static final String XLSX_SCHEMA_SCAFFOLD_STG_PATH = "xlsxschema/xlsxSchemaScaffoldTemplate.stg";
    private static final String XLSX_SCHEMA_SCAFFOLD_TXT_PATH = "xlsxschema/xlsxSchemaScaffoldTemplate.txt";

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getTarget()) ? dto.getTarget() : ""
                , Strings.isNotEmpty(dto.getSettingName()) ? dto.getSettingName() : ""
                , Strings.isNotEmpty(dto.getUnitSettingName()) ? dto.getUnitSettingName() : ""
                , Strings.isNotEmpty(dto.getTemplateName()) ? dto.getTemplateName() : ""
                , Strings.isNotEmpty(dto.getParameterName()) ? dto.getParameterName() : ""
                , Strings.isNotEmpty(dto.getCommandType()) ? dto.getCommandType() : ""
                , dto.getCommandInput()
                , new DataSetLoadOption("dataset", dto.getDatasetDto(), true)
                , dto.getDatasetType() != null ? dto.getDatasetType() : ResultType.csv
                , Strings.isNotEmpty(dto.getDatasetEncoding()) ? dto.getDatasetEncoding() : "UTF-8"
        );
    }

    public static ScaffoldDto toDto(final String[] args) {
        final ScaffoldDto dto = new ScaffoldDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        dto.setCommandInput(Arrays.stream(args)
                                  .filter(it -> it.startsWith(COMMAND_INPUT_PREFIX))
                                  .map(it -> "-" + it.substring(COMMAND_INPUT_PREFIX.length()))
                                  .toArray(String[]::new));
        final String[] datasetArgs = Arrays.stream(args)
                                              .filter(it -> it.startsWith("-dataset."))
                                              .toArray(String[]::new);
        new ArgumentMapper("dataset").populate(datasetArgs, dto.getDatasetDto());
        return dto;
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final File settingDir = new File(baseDir, FileResources.RESOURCES_SETTING_PATH);
        final File templateDir = new File(baseDir, FileResources.RESOURCES_TEMPLATE_PATH);
        final File paramDir = new File(baseDir, "option");
        final boolean generateDdl = GenerateType.ddl.name().equals(this.target);
        final boolean generateJavaBean = GenerateType.javaBean.name().equals(this.target);
        final boolean generateXlsxSchema = GenerateType.xlsxSchema.name().equals(this.target);
        final boolean generateFixedColumnDef = GenerateType.fixedColumnDef.name().equals(this.target);
        final boolean generateParameter = "parameter".equals(this.target)
                && Strings.isNotEmpty(this.commandType);
        if (generateJavaBean || generateDdl) {
            GenerateType generateType = GenerateType.valueOf(this.target);
            this.copySettingResource(settingDir, generateType, this.settingName);
            this.copySettingResource(settingDir, generateType, this.unitSettingName);
            if (Strings.isNotEmpty(this.templateName)) {
                if (templateDir.mkdirs() || templateDir.isDirectory()) {
                    this.copyClasspathResource(generateType.getStgPath(),
                                               new File(templateDir, this.templateName + ".stg"));
                    this.copyClasspathResource(generateType.getTemplatePath(),
                                               new File(templateDir, this.templateName + ".txt"));
                }
            }
            if (this.hasDataset()) {
                this.writeDatasetSrcFiles(new File(baseDir, DATASET_SRC_DIR));
            }
            if (Strings.isNotEmpty(this.parameterName)) {
                if (paramDir.mkdirs() || paramDir.isDirectory()) {
                    this.writeGenericParamFile(paramDir, generateDdl);
                }
            }
        }
        if ((generateXlsxSchema || generateFixedColumnDef) && this.hasDataset()) {
            final String name = Strings.isNotEmpty(this.templateName) ? this.templateName : this.target;
            final String unitSettingFileName =
                    Strings.isNotEmpty(this.unitSettingName) ? this.unitSettingName : this.target;
            if (settingDir.mkdirs() || settingDir.isDirectory()) {
                this.copyClasspathResource(
                        generateXlsxSchema ? ScaffoldOption.XLSX_SCHEMA_SAMPLE_SETTINGS_PATH
                                : ScaffoldOption.FIXED_COLUMN_DEF_SAMPLE_SETTINGS_PATH,
                        new File(settingDir, unitSettingFileName + ".json"));
            }
            final Column[] datasetColumns =
                    generateXlsxSchema ? XLSX_SCHEMA_DATASET_COLUMNS : FIXED_COLUMN_DEF_DATASET_COLUMNS;
            this.writeDatasetSrcFiles(new File(baseDir, DATASET_SRC_DIR), datasetColumns,
                                      generateXlsxSchema ? this::buildXlsxSchemaRow
                                              : (col, tbl, idx) -> this.buildFixedColumnDefRow(col));
            if (templateDir.mkdirs() || templateDir.isDirectory()) {
                this.writeSchemaTemplate(templateDir, name, generateXlsxSchema);
            }
            if (Strings.isNotEmpty(this.parameterName)) {
                if (paramDir.mkdirs() || paramDir.isDirectory()) {
                    this.writeSchemaParamFile(paramDir, name, unitSettingFileName, datasetColumns);
                }
            }
        }
        if (generateParameter) {
            if (paramDir.mkdirs() || paramDir.isDirectory()) {
                final String[] shrunkArgs = new CommandParameters(Type.valueOf(this.commandType), this.commandInput)
                        .shrink().args();
                Files.write(new File(paramDir, this.commandType + ".param").toPath(),
                            Arrays.asList(shrunkArgs), StandardCharsets.UTF_8);
            }
        }
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir);
    }

    @Override
    public ScaffoldDto toDto() {
        return ScaffoldOption.toDto(this.toArgs(true));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder();
        result.putDir("-result", this.resultDir, BaseDir.RESULT)
              .put("-target", this.target);
        result.put("-setting", this.settingName)
              .put("-unitSetting", this.unitSettingName)
              .put("-template", this.templateName)
              .put("-parameter", this.parameterName);
        result.put("-commandType", this.commandType);
        Arrays.stream(this.commandInput)
              .filter(arg -> arg.startsWith("-"))
              .forEach(arg -> {
                  final int eqIdx = arg.indexOf('=');
                  final String key = COMMAND_INPUT_PREFIX + arg.substring(1, eqIdx > 0 ? eqIdx : arg.length());
                  result.put(key, eqIdx > 0 ? arg.substring(eqIdx + 1) : "true");
              });
        if (this.hasDataset()) {
            result.put("-datasetType", this.datasetType, ResultType.class);
            result.put("-datasetEncoding", this.datasetEncoding);
            result.addComponent("dataset", this.dataset.toParametersBuilder().build());
        }
        return result;
    }

    private DataSourceType resolveDataSourceType() {
        return switch (this.datasetType) {
            case csv, xls, xlsx, fixed, table -> this.datasetType.toDataSourceType();
            default -> null;
        };
    }

    private boolean hasDataset() {
        return this.dataset != null
                && this.dataset.srcType() != null
                && this.dataset.srcType() != DataSourceType.none
                && Strings.isNotEmpty(this.dataset.src());
    }

    private boolean isHeaderlessDataset() {
        return switch (this.datasetType) {
            case format, fixed -> true;
            case csv, xls, xlsx, table -> false;
        };
    }

    private boolean usesDatasetEncoding() {
        return switch (this.datasetType) {
            case csv, format, fixed -> true;
            case xls, xlsx, table -> false;
        };
    }

    @FunctionalInterface
    private interface DatasetRowBuilder {
        Object[] build(String columnName, String tableName, int columnIndex);
    }

    private void writeDatasetSrcFiles(final File srcDir) throws IOException {
        this.writeDatasetSrcFiles(srcDir, DDL_SCHEMA_COLUMNS, (col, tbl, idx) -> this.buildSchemaRow(col, tbl));
    }

    private void writeDatasetSrcFiles(final File srcDir, final Column[] schemaColumns,
                                       final DatasetRowBuilder rowBuilder) throws IOException {
        if (!srcDir.mkdirs() && !srcDir.isDirectory()) {
            return;
        }
        final ComparableDataSet dataSet = this.loadDatasetForSrc();
        final IDataSetConverter converter = this.createSrcConverter(srcDir);
        try {
            converter.startDataSet();
            for (final String tableName : dataSet.getTableNames()) {
                final Column[] sourceColumns = dataSet.getTable(tableName).getTableMetaData().getColumns();
                final DefaultTable schemaTable = new DefaultTable(tableName, schemaColumns);
                int columnIndex = 0;
                for (final Column column : sourceColumns) {
                    schemaTable.addRow(rowBuilder.build(column.getColumnName(), tableName, columnIndex++));
                }
                converter.convert(schemaTable);
            }
            converter.endDataSet();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private ComparableDataSet loadDatasetForSrc() {
        final ComparableDataSetParam param = this.dataset.getParam()
                .setLoadData(false)
                .build();
        return new ComparableDataSetLoader(this.parameter).loadDataSet(param);
    }

    private IDataSetConverter createSrcConverter(final File srcDir) {
        final DataSetConverterParam converterParam = DataSetConverterParam.builder()
                .setResultType(this.datasetType)
                .setResultDir(srcDir)
                .setExportEmptyTable(true)
                .setSkipHeader(!this.isHeaderlessDataset())
                .setOutputEncoding(this.datasetEncoding)
                .build();
        return new DataSetConverterLoader().get(converterParam);
    }

    private Object[] buildSchemaRow(final String columnName, final String tableName) {
        // order must match DDL_SCHEMA_COLUMNS: COLUMN_NAME, TYPE_NAME, COLUMN_SIZE, DECIMAL_DIGITS,
        // NULLABLE, IS_PK, PK_NAME, REMARKS, TABLE_REMARKS, TABLE_NAME, PACKAGE
        return new Object[]{columnName, "", "", "", "", "", "", "", "", tableName, ""};
    }

    private Object[] buildXlsxSchemaRow(final String columnName, final String tableName, final int columnIndex) {
        // order must match XLSX_SCHEMA_DATASET_COLUMNS: COLUMN_NAME, IS_PK, SHEET_NAME, DATA_START,
        // COLUMN_INDEX, CELL_ADDRESS.
        // SHEET_NAME/DATA_START are per-table, not per-column, but every row is seeded with the same
        // value since the template (rowEntry()) only reads first(rows).SHEET_NAME/first(rows).DATA_START:
        // edit them consistently across a table's rows, since only the first row's value takes effect.
        final String cellAddress =
                new CellReference(XLSX_SCHEMA_DEFAULT_DATA_START, columnIndex).formatAsString();
        return new Object[]{columnName, "", tableName, String.valueOf(XLSX_SCHEMA_DEFAULT_DATA_START),
                String.valueOf(columnIndex), cellAddress};
    }

    private Object[] buildFixedColumnDefRow(final String columnName) {
        // order must match FIXED_COLUMN_DEF_DATASET_COLUMNS: name, length, align, pad
        return new Object[]{columnName, "10", "left", " "};
    }

    private void writeGenericParamFile(final File paramDir, final boolean isDdl) throws IOException {
        final ParametersBuilder builder = new ParametersBuilder();
        if (Strings.isNotEmpty(this.templateName)) {
            // A custom template is driven through generateType=txt + unitSetting, reusing the same
            // rows/tableName/dataset.PK attributes the built-in ddl/javaBean templates already rely on.
            this.putTemplateGenerationParams(builder, this.templateName);
            builder.put("-resultPath", isDdl ? "$param.tableName$.sql"
                    : new TemplateRender.Builder().build().getAttributeName("tableName", "snakeToUpperCamel") + ".java");
        } else {
            builder.put("-generateType", isDdl ? GenerateType.ddl.name() : GenerateType.javaBean.name(), false);
        }
        if (Strings.isNotEmpty(this.settingName)) {
            builder.put("-setting", "resources/setting/" + this.settingName + ".json");
        }
        if (Strings.isNotEmpty(this.unitSettingName)) {
            builder.put("-unitSetting", "resources/setting/" + this.unitSettingName + ".json");
        }
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        if (this.hasDataset()) {
            this.addDatasetSrcParams(builder);
            if (this.isHeaderlessDataset()) {
                builder.put("-headerName", ScaffoldOption.DDL_SCHEMA_HEADER_NAMES);
            }
        }
        Files.write(new File(paramDir, this.parameterName + ".param").toPath(),
                    builder.build().toList(false), StandardCharsets.UTF_8);
    }

    private void putTemplateGenerationParams(final ParametersBuilder builder, final String templateFileName) {
        builder.put("-generateType", GenerateType.txt.name(), false);
        builder.put("-unit", ParameterUnit.table.name(), false);
        builder.put("-template", "resources/template/" + templateFileName + ".txt");
        builder.put("-template.templateGroup", "resources/template/" + templateFileName + ".stg");
    }

    private void addDatasetSrcParams(final ParametersBuilder builder) {
        builder.put("-src.src", DATASET_SRC_DIR);
        final DataSourceType srcType = this.resolveDataSourceType();
        if (srcType != null) {
            builder.put("-src.srcType", srcType.name());
        }
        if (this.usesDatasetEncoding()) {
            builder.put("-encoding", this.datasetEncoding);
        }
    }

    private void writeSchemaTemplate(final File templateDir, final String name, final boolean isXlsxSchema)
            throws IOException {
        this.copyClasspathResource(
                isXlsxSchema ? ScaffoldOption.XLSX_SCHEMA_SCAFFOLD_STG_PATH : GenerateType.fixedColumnDef.getStgPath(),
                new File(templateDir, name + ".stg"));
        this.copyClasspathResource(
                isXlsxSchema ? ScaffoldOption.XLSX_SCHEMA_SCAFFOLD_TXT_PATH
                        : ScaffoldOption.FIXED_COLUMN_DEF_SCAFFOLD_TXT_PATH,
                new File(templateDir, name + ".txt"));
    }

    private void writeSchemaParamFile(final File paramDir, final String templateFileName,
                                       final String unitSettingFileName, final Column[] datasetColumns) throws IOException {
        final ParametersBuilder builder = new ParametersBuilder();
        this.putTemplateGenerationParams(builder, templateFileName);
        builder.put("-unitSetting", "resources/setting/" + unitSettingFileName + ".json");
        builder.put("-resultPath", "$param.tableName$.json");
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        this.addDatasetSrcParams(builder);
        if (this.isHeaderlessDataset()) {
            builder.put("-headerName", ScaffoldOption.headerNames(datasetColumns));
        }
        Files.write(new File(paramDir, this.parameterName + ".param").toPath(),
                    builder.build().toList(false), StandardCharsets.UTF_8);
    }

    private void copySettingResource(final File settingDir, final GenerateType generateType, final String name) throws IOException {
        if (Strings.isEmpty(name)) {
            return;
        }
        if (settingDir.mkdirs() || settingDir.isDirectory()) {
            this.copyClasspathResource(generateType.defaultSettingsPath(), new File(settingDir, name + ".json"));
        }
    }

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String headerNames(final Column[] columns) {
        return Arrays.stream(columns)
                .map(Column::getColumnName)
                .collect(Collectors.joining(","));
    }
}
