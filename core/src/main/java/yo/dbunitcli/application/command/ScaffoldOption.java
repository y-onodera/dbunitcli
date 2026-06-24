package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public record ScaffoldOption(
        Parameter parameter
        , String resultDir
        , String sqlFilePrefix
        , String sqlFileSuffix
        , List<String> generateTargets
        , DataSetLoadOption srcData
        , DataSetConverterOption datasetResult
) implements CommandLineOption<ScaffoldDto> {

    public static ScaffoldDto toDto(final String[] args) {
        final ScaffoldDto dto = new ScaffoldDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        new ArgumentMapper("src").populate(args, dto.getSrcData());
        new ArgumentMapper("datasetResult").populate(args, dto.getDatasetResult());
        return dto;
    }

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : ""
                , Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : ""
                , dto.getGenerateTargets() != null ? dto.getGenerateTargets() : List.of()
                , new DataSetLoadOption("src", dto.getSrcData())
                , new DataSetConverterOption("datasetResult", dto.getDatasetResult())
        );
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final File settingDir = new File(baseDir, "resources/setting");
        final File templateDir = new File(baseDir, "resources/template");
        final File paramDir = new File(baseDir, "resources/param");
        settingDir.mkdirs();
        templateDir.mkdirs();
        paramDir.mkdirs();
        final boolean allTargets = this.generateTargets.isEmpty();
        final boolean generateDdl = allTargets || this.generateTargets.contains("ddl");
        final boolean generateJavaBean = allTargets || this.generateTargets.contains("javaBean");
        if (generateJavaBean) {
            this.copyClasspathResource("javabean/javaBeanSettings.json", new File(settingDir, "scaffold.json"));
        }
        if (generateDdl) {
            this.copyClasspathResource("sql/ddlTemplate.stg", new File(templateDir, "ddl.stg"));
            this.copyClasspathResource("sql/ddlTemplate.txt", new File(templateDir, "ddl.txt"));
        }
        if (generateJavaBean) {
            this.copyClasspathResource("javabean/javaBeanTemplate.stg", new File(templateDir, "javaBean.stg"));
            this.copyClasspathResource("javabean/javaBeanTemplate.txt", new File(templateDir, "javaBean.txt"));
        }
        if (generateDdl || generateJavaBean) {
            final ComparableDataSetParam.Builder paramBuilder = this.srcData.getParam()
                    .setUseJdbcMetaData(true)
                    .setLoadData(false);
            if (this.datasetResult.resultType() != null) {
                paramBuilder.setConverter(new DataSetConverterLoader().get(this.datasetResult.getParam().build()));
            }
            final ComparableDataSet dataSet = this.getComparableDataSetLoader().loadDataSet(paramBuilder.build());
            for (final String tableName : dataSet.getTableNames()) {
                if (generateDdl) {
                    this.writeParamFile(paramDir, tableName, "ddl");
                }
                if (generateJavaBean) {
                    this.writeParamFile(paramDir, tableName, "javaBean");
                }
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
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        srcComponent.remove("-src.loadData").remove("-src.useJdbcMetaData");
        result.addComponent("srcData", srcComponent.build());
        result.addComponent("datasetResult", this.datasetResult.toParameters());
        result.putDir("-result", this.resultDir, BaseDir.RESULT)
              .put("-sqlFilePrefix", this.sqlFilePrefix)
              .put("-sqlFileSuffix", this.sqlFileSuffix);
        if (!this.generateTargets.isEmpty()) {
            result.put("-generateTargets", String.join(",", this.generateTargets));
        }
        return result;
    }

    private List<String> paramLines(final String genType, final String tableName) {
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", genType, false);
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        srcComponent.remove("-src.loadData")
                    .remove("-src.useJdbcMetaData")
                    .put("-setting", "resources/setting/scaffold.json");
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

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
