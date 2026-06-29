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

public record ScaffoldOption(
        Parameter parameter
        , String resultDir
        , String target
        , String settingName
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
            new Column("PACKAGE", DataType.VARCHAR)
    };

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getTarget()) ? dto.getTarget() : ""
                , Strings.isNotEmpty(dto.getSettingName()) ? dto.getSettingName() : ""
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
        new ArgumentMapper("dataset").populate(args, dto.getDatasetDto());
        return dto;
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final File settingDir = new File(baseDir, FileResources.RESOURCES_SETTING_PATH);
        final File templateDir = new File(baseDir, FileResources.RESOURCES_TEMPLATE_PATH);
        final File paramDir = new File(baseDir, "option");
        final boolean generateDdl = GenerateType.ddl.name().equals(this.target);
        final boolean generateJavaBean = GenerateType.javaBean.name().equals(this.target);
        final boolean generateParameter = "parameter".equals(this.target)
                && Strings.isNotEmpty(this.commandType);
        if (generateJavaBean || generateDdl) {
            GenerateType generateType = GenerateType.valueOf(this.target);
            if (Strings.isNotEmpty(this.settingName)) {
                if (settingDir.mkdirs() || settingDir.isDirectory()) {
                    this.copyClasspathResource(generateType.defaultSettingsPath(),
                                               new File(settingDir, this.settingName + ".json"));
                }
            }
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

    private void writeDatasetSrcFiles(final File srcDir) throws IOException {
        if (!srcDir.mkdirs() && !srcDir.isDirectory()) {
            return;
        }
        final ComparableDataSetParam param = this.dataset.getParam()
                .setLoadData(false)
                .build();
        final ComparableDataSet dataSet = new ComparableDataSetLoader(this.parameter).loadDataSet(param);
        final DataSetConverterParam converterParam = DataSetConverterParam.builder()
                .setResultType(this.datasetType)
                .setResultDir(srcDir)
                .setExportEmptyTable(true)
                .setSkipHeader(true)
                .setOutputEncoding(this.datasetEncoding)
                .build();
        final IDataSetConverter converter = new DataSetConverterLoader().get(converterParam);
        try {
            converter.startDataSet();
            for (final String tableName : dataSet.getTableNames()) {
                final Column[] sourceColumns = dataSet.getTable(tableName).getTableMetaData().getColumns();
                final DefaultTable schemaTable = new DefaultTable(tableName, DDL_SCHEMA_COLUMNS);
                for (final Column column : sourceColumns) {
                    schemaTable.addRow(new Object[]{column.getColumnName(), "", "", "", "", "", "", "", "", ""});
                }
                converter.convert(schemaTable);
            }
            converter.endDataSet();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private void writeGenericParamFile(final File paramDir, final boolean isDdl) throws IOException {
        final boolean hasTemplate = Strings.isNotEmpty(this.templateName);
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", hasTemplate ? GenerateType.txt.name() :
                (isDdl ? GenerateType.ddl.name() : GenerateType.javaBean.name()), false);
        if (hasTemplate) {
            builder.put("-template", "resources/template/" + this.templateName + ".stg");
        }
        if (Strings.isNotEmpty(this.settingName)) {
            builder.put("-setting", "resources/setting/" + this.settingName + ".json");
        }
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        if (this.hasDataset()) {
            builder.put("-src.src", DATASET_SRC_DIR);
            final DataSourceType srcType = this.resolveDataSourceType();
            if (srcType != null) {
                builder.put("-src.srcType", srcType.name());
            }
        }
        Files.write(new File(paramDir, this.parameterName + ".param").toPath(),
                    builder.build().toList(false), StandardCharsets.UTF_8);
    }

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
