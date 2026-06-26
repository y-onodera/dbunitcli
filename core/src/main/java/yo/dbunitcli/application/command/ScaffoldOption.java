package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.common.Parameter;
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
) implements CommandLineOption<ScaffoldDto> {

    private static final String COMMAND_INPUT_PREFIX = "-commandInput.";

    public static ScaffoldDto toDto(final String[] args) {
        final ScaffoldDto dto = new ScaffoldDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        dto.setCommandInput(Arrays.stream(args)
                .filter(it -> it.startsWith(COMMAND_INPUT_PREFIX))
                .map(it -> "-" + it.substring(COMMAND_INPUT_PREFIX.length()))
                .toArray(String[]::new));
        return dto;
    }

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getTarget()) ? dto.getTarget() : ""
                , Strings.isNotEmpty(dto.getSettingName()) ? dto.getSettingName() : ""
                , Strings.isNotEmpty(dto.getTemplateName()) ? dto.getTemplateName() : ""
                , Strings.isNotEmpty(dto.getParameterName()) ? dto.getParameterName() : ""
                , Strings.isNotEmpty(dto.getCommandType()) ? dto.getCommandType() : ""
                , dto.getCommandInput()
        );
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final File settingDir = new File(baseDir, "resources/setting");
        final File templateDir = new File(baseDir, "resources/template");
        final File paramDir = new File(baseDir, "resources/param");
        final boolean generateDdl = "ddl".equals(this.target);
        final boolean generateJavaBean = "javaBean".equals(this.target);
        final boolean generateParameter = "parameter".equals(this.target)
                && Strings.isNotEmpty(this.commandType);
        if (generateJavaBean) {
            if (Strings.isNotEmpty(this.settingName)) {
                settingDir.mkdirs();
                this.copyClasspathResource("javabean/javaBeanSettings.json", new File(settingDir, this.settingName + ".json"));
            }
            if (Strings.isNotEmpty(this.templateName)) {
                templateDir.mkdirs();
                this.copyClasspathResource("javabean/javaBeanTemplate.stg", new File(templateDir, this.templateName + ".stg"));
                this.copyClasspathResource("javabean/javaBeanTemplate.txt", new File(templateDir, this.templateName + ".txt"));
            }
        }
        if (generateDdl) {
            if (Strings.isNotEmpty(this.settingName)) {
                settingDir.mkdirs();
                this.copyClasspathResource("sql/ddlSettings.json", new File(settingDir, this.settingName + ".json"));
            }
            if (Strings.isNotEmpty(this.templateName)) {
                templateDir.mkdirs();
                this.copyClasspathResource("sql/ddlTemplate.stg", new File(templateDir, this.templateName + ".stg"));
                this.copyClasspathResource("sql/ddlTemplate.txt", new File(templateDir, this.templateName + ".txt"));
            }
        }
        if ((generateDdl || generateJavaBean) && Strings.isNotEmpty(this.parameterName)) {
            paramDir.mkdirs();
            this.writeGenericParamFile(paramDir, generateDdl);
        }
        if (generateParameter) {
            paramDir.mkdirs();
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
        return result;
    }

    private void writeGenericParamFile(final File paramDir, final boolean isDdl) throws IOException {
        final boolean hasTemplate = Strings.isNotEmpty(this.templateName);
        final ParametersBuilder builder = new ParametersBuilder();
        builder.put("-generateType", hasTemplate ? "txt" : (isDdl ? "ddl" : "javaBean"), false);
        if (hasTemplate) {
            builder.put("-template", "resources/template/" + this.templateName + ".stg");
        }
        if (Strings.isNotEmpty(this.settingName)) {
            builder.put("-setting", "resources/setting/" + this.settingName + ".json");
        }
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        Files.write(new File(paramDir, this.parameterName + ".param").toPath(),
                builder.build().toList(false), StandardCharsets.UTF_8);
    }

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
