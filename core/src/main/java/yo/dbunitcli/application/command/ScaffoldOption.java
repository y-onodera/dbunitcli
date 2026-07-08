package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
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
    private static final String DDL_SCHEMA_HEADER_NAMES = Arrays.stream(DDL_SCHEMA_COLUMNS)
            .map(Column::getColumnName)
            .collect(Collectors.joining(","));

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
            this.copySettingResource(settingDir, GenerateType.valueOf(this.target), this.unitSettingName);
            this.writeTableColumnsSrcFiles(new File(baseDir, DATASET_SRC_DIR));
            if (Strings.isNotEmpty(this.parameterName)) {
                if (paramDir.mkdirs() || paramDir.isDirectory()) {
                    this.writeSchemaParamFile(paramDir, generateXlsxSchema);
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

    private void writeDatasetSrcFiles(final File srcDir) throws IOException {
        if (!srcDir.mkdirs() && !srcDir.isDirectory()) {
            return;
        }
        final ComparableDataSet dataSet = this.loadDatasetForSrc();
        final IDataSetConverter converter = this.createSrcConverter(srcDir);
        try {
            converter.startDataSet();
            for (final String tableName : dataSet.getTableNames()) {
                final Column[] sourceColumns = dataSet.getTable(tableName).getTableMetaData().getColumns();
                final DefaultTable schemaTable = new DefaultTable(tableName, DDL_SCHEMA_COLUMNS);
                for (final Column column : sourceColumns) {
                    schemaTable.addRow(this.buildSchemaRow(column.getColumnName(), tableName));
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

    private void writeGenericParamFile(final File paramDir, final boolean isDdl) throws IOException {
        final boolean hasTemplate = Strings.isNotEmpty(this.templateName);
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", isDdl ? GenerateType.ddl.name() : GenerateType.javaBean.name(), false);
        if (hasTemplate) {
            builder.put("-template", "resources/template/" + this.templateName + ".txt");
            builder.put("-template.templateGroup", "resources/template/" + this.templateName + ".stg");
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

    private void writeTableColumnsSrcFiles(final File srcDir) throws IOException {
        if (!srcDir.mkdirs() && !srcDir.isDirectory()) {
            return;
        }
        final ComparableDataSet dataSet = this.loadDatasetForSrc();
        final IDataSetConverter converter = this.createSrcConverter(srcDir);
        converter.startDataSet();
        for (final String tableName : dataSet.getTableNames()) {
            converter.convert(dataSet.getTable(tableName));
        }
        converter.endDataSet();
    }

    private void writeSchemaParamFile(final File paramDir, final boolean isXlsxSchema) throws IOException {
        final GenerateType generateType = isXlsxSchema ? GenerateType.xlsxSchema : GenerateType.fixedColumnDef;
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", generateType.name(), false);
        if (isXlsxSchema) {
            builder.put("-resultPath", this.parameterName + ".json");
        }
        if (Strings.isNotEmpty(this.unitSettingName)) {
            builder.put("-unitSetting", "resources/setting/" + this.unitSettingName + ".json");
        }
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        this.addDatasetSrcParams(builder);
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
}
