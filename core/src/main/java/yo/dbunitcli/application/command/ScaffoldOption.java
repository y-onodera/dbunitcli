package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public record ScaffoldOption(
        Parameter parameter
        , String resultDir
        , String sqlFilePrefix
        , String sqlFileSuffix
        , String target
        , List<String> ddlIncludes
        , List<String> javaBeanIncludes
        , String commandType
        , String[] commandInput
        , DataSetLoadOption srcData
        , DataSetConverterOption datasetResult
) implements CommandLineOption<ScaffoldDto> {

    private static final String COMMAND_INPUT_PREFIX = "-commandInput.";

    public static ScaffoldDto toDto(final String[] args) {
        final ScaffoldDto dto = new ScaffoldDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        new ArgumentMapper("src").populate(args, dto.getSrcData());
        new ArgumentMapper("datasetResult").populate(args, dto.getDatasetResult());
        dto.setCommandInput(Arrays.stream(args)
                .filter(it -> it.startsWith(COMMAND_INPUT_PREFIX))
                .map(it -> "-" + it.substring(COMMAND_INPUT_PREFIX.length()))
                .toArray(String[]::new));
        return dto;
    }

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : ""
                , Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : ""
                , Strings.isNotEmpty(dto.getTarget()) ? dto.getTarget() : ""
                , dto.getDdlIncludes() != null ? dto.getDdlIncludes() : List.of()
                , dto.getJavaBeanIncludes() != null ? dto.getJavaBeanIncludes() : List.of()
                , Strings.isNotEmpty(dto.getCommandType()) ? dto.getCommandType() : ""
                , dto.getCommandInput()
                , new DataSetLoadOption("src", ScaffoldOption.srcDataWithDefault(dto), true)
                , new DataSetConverterOption("datasetResult", dto.getDatasetResult())
        );
    }

    private static DataSetLoadDto srcDataWithDefault(final ScaffoldDto dto) {
        final DataSetLoadDto srcData = dto.getSrcData();
        if (srcData.getSrcType() == null) {
            srcData.setSrcType(DataSourceType.none);
        }
        return srcData;
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final File settingDir = new File(baseDir, "resources/setting");
        final File templateDir = new File(baseDir, "resources/template");
        final File paramDir = new File(baseDir, "resources/param");
        settingDir.mkdirs();
        templateDir.mkdirs();
        paramDir.mkdirs();
        final boolean generateDdl = "ddl".equals(this.target);
        final boolean generateJavaBean = "javaBean".equals(this.target);
        final boolean generateParameter = "parameter".equals(this.target)
                && Strings.isNotEmpty(this.commandType);
        if (generateJavaBean) {
            if (this.includes(this.javaBeanIncludes, "setting")) {
                this.copyClasspathResource("javabean/javaBeanSettings.json", new File(settingDir, "javaBean.json"));
            }
            if (this.includes(this.javaBeanIncludes, "template")) {
                this.copyClasspathResource("javabean/javaBeanTemplate.stg", new File(templateDir, "javaBean.stg"));
                this.copyClasspathResource("javabean/javaBeanTemplate.txt", new File(templateDir, "javaBean.txt"));
            }
        }
        if (generateDdl) {
            if (this.includes(this.ddlIncludes, "setting")) {
                this.copyClasspathResource("sql/ddlSettings.json", new File(settingDir, "ddl.json"));
            }
            if (this.includes(this.ddlIncludes, "template")) {
                this.copyClasspathResource("sql/ddlTemplate.stg", new File(templateDir, "ddl.stg"));
                this.copyClasspathResource("sql/ddlTemplate.txt", new File(templateDir, "ddl.txt"));
            }
        }
        if (generateDdl || generateJavaBean) {
            final boolean needDdlParam = generateDdl && this.includes(this.ddlIncludes, "parameter");
            final boolean needJavaBeanParam = generateJavaBean && this.includes(this.javaBeanIncludes, "parameter");
            if (needDdlParam || needJavaBeanParam) {
                if (this.srcData.srcType() != DataSourceType.none) {
                    final ComparableDataSetParam.Builder paramBuilder = this.srcData.getParam()
                            .setUseJdbcMetaData(true)
                            .setLoadData(false);
                    final ComparableDataSet dataSet = this.getComparableDataSetLoader().loadDataSet(paramBuilder.build());
                    for (final String tableName : dataSet.getTableNames()) {
                        if (needDdlParam) {
                            this.writeParamFile(paramDir, tableName, "ddl");
                        }
                        if (needJavaBeanParam) {
                            this.writeParamFile(paramDir, tableName, "javaBean");
                        }
                    }
                } else {
                    if (needDdlParam) {
                        this.writeGenericParamFile(paramDir, "ddl");
                    }
                    if (needJavaBeanParam) {
                        this.writeGenericParamFile(paramDir, "javaBean");
                    }
                }
            }
        }
        if (generateParameter) {
            final String[] shrunkArgs = new CommandParameters(Type.valueOf(this.commandType), this.commandInput)
                    .shrink().args();
            Files.write(new File(paramDir, this.commandType + ".param").toPath(),
                    Arrays.asList(shrunkArgs), StandardCharsets.UTF_8);
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
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        srcComponent.remove("-src.loadData").remove("-src.useJdbcMetaData");
        result.addComponent("srcData", srcComponent.build());
        result.addComponent("datasetResult", this.datasetResult.toParameters());
        result.putDir("-result", this.resultDir, BaseDir.RESULT)
              .put("-sqlFilePrefix", this.sqlFilePrefix)
              .put("-sqlFileSuffix", this.sqlFileSuffix)
              .put("-target", this.target);
        if (!this.ddlIncludes.isEmpty()) {
            result.put("-ddlIncludes", String.join(",", this.ddlIncludes));
        }
        if (!this.javaBeanIncludes.isEmpty()) {
            result.put("-javaBeanIncludes", String.join(",", this.javaBeanIncludes));
        }
        result.put("-commandType", this.commandType);
        Arrays.stream(this.commandInput)
              .filter(arg -> arg.startsWith("-"))
              .forEach(arg -> {
                  final int eqIdx = arg.indexOf('=');
                  final String key = COMMAND_INPUT_PREFIX + arg.substring(1, eqIdx > 0 ? eqIdx : arg.length());
                  result.put(key, eqIdx > 0 ? arg.substring(eqIdx + 1) : "true");
              });
        return result;
    }

    private List<String> paramLines(final String genType, final String tableName) {
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", genType, false);
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        srcComponent.remove("-src.loadData")
                    .remove("-src.useJdbcMetaData")
                    .put("-setting", "resources/setting/" + genType + ".json");
        builder.addComponent("srcData", srcComponent.build());
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        if ("ddl".equals(genType)) {
            builder.put("-sqlFilePrefix", this.sqlFilePrefix)
                   .put("-sqlFileSuffix", this.sqlFileSuffix);
        }
        builder.put("-regTableInclude", tableName);
        return builder.build().toList(false);
    }

    private void writeParamFile(final File paramDir, final String tableName, final String genType)
            throws IOException {
        Files.write(new File(paramDir, tableName + "_" + genType + ".param").toPath(),
                this.paramLines(genType, tableName), StandardCharsets.UTF_8);
    }

    private void writeGenericParamFile(final File paramDir, final String genType) throws IOException {
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", genType, false);
        builder.put("-setting", "resources/setting/" + genType + ".json");
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        if ("ddl".equals(genType)) {
            builder.put("-sqlFilePrefix", this.sqlFilePrefix)
                   .put("-sqlFileSuffix", this.sqlFileSuffix);
        }
        Files.write(new File(paramDir, genType + ".param").toPath(),
                builder.build().toList(false), StandardCharsets.UTF_8);
    }

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private boolean includes(final List<String> includes, final String item) {
        return includes.isEmpty() || includes.contains(item);
    }
}
