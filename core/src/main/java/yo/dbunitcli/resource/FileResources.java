package yo.dbunitcli.resource;

import yo.dbunitcli.Strings;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public record FileResources() {

    public static final String PROPERTY_WORKSPACE = "yo.dbunit.cli.workspace";
    public static final String PROPERTY_DATASET_BASE = "yo.dbunit.cli.dataset.base";
    public static final String PROPERTY_RESULT_BASE = "yo.dbunit.cli.result.base";
    public static final String RESOURCES_SETTING_PATH = "resources/setting";
    public static final String RESOURCES_TEMPLATE_PATH = "resources/template";
    public static final String RESOURCES_JDBC_PATH = "resources/jdbc";
    public static final String RESOURCES_XLSX_SCHEMA_PATH = "resources/xlsxSchema";
    public static final String OPTION_PARAMETERIZE_TEMPLATE_PATH = "option/parameterize/template";

    public static String readClasspathResource(final String aURL) {
        return FileResources.readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(final String aURL, final Charset aCharset) {
        try (final InputStream inputStream = FileResources.class.getClassLoader().getResourceAsStream(aURL);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), aCharset))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String read(final File aFile, final String aEncoding) {
        try {
            return java.nio.file.Files.readString(aFile.toPath(), Charset.forName(aEncoding));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public static File searchWorkspace(final String path) {
        return FileResources.searchInOrder(path, baseDir().toPath().normalize().toString(), File::new);
    }

    public static File searchDatasetBase(final String path) {
        return FileResources.searchInOrder(path, datasetDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    public static File searchSetting(final String settingPath) {
        return FileResources.searchInOrder(settingPath, settingDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    public static File searchTemplate(final String templatePath) {
        if (Strings.isEmpty(templatePath)) {
            return null;
        }
        return FileResources.searchInOrder(templatePath, templateFileDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    public static File searchJdbc(final String jdbcPath) {
        return FileResources.searchInOrder(jdbcPath, jdbcPropDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    public static File searchXlsxSchema(final String xlsxSchemaPath) {
        if (Strings.isEmpty(xlsxSchemaPath)) {
            return null;
        }
        return FileResources.searchInOrder(xlsxSchemaPath, xlsxSchemaDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    public static File fixedColumnDefDir() {
        return new File(baseDir(), "resources/fixed-column-def");
    }

    public static File searchFixedColumnDef(final String path) {
        if (Strings.isEmpty(path)) {
            return null;
        }
        return FileResources.searchInOrder(path, fixedColumnDefDir().toPath().normalize().toString(), FileResources::searchWorkspace);
    }

    private static String expandEnvironmentVariables(final String path) {
        if (Strings.isEmpty(path)) {
            return path;
        }
        String expandedPath = path;
        for (final String varName : new String[]{"USERPROFILE", "HOME"}) {
            final String placeholder = "%" + varName + "%";
            if (expandedPath.contains(placeholder)) {
                final String value = System.getenv(varName);
                if (Strings.isNotEmpty(value)) {
                    expandedPath = expandedPath.replace(placeholder, value);
                }
            }
        }
        return expandedPath;
    }

    private static File searchInOrder(final String path, final String parent, final Function<String, File> next) {
        final String expandedPath = expandEnvironmentVariables(path);
        if (new File(expandedPath).isAbsolute()) {
            return new File(expandedPath);
        }
        if (Strings.isNotEmpty(parent)) {
            final File candidate = new File(parent, expandedPath);
            if (candidate.exists()) {
                return candidate;
            }
        }
        return next.apply(expandedPath);
    }

    public static File resultDir(final String path) {
        if (Strings.isEmpty(path)) {
            return FileResources.resultDir();
        }
        final String expandedPath = expandEnvironmentVariables(path);
        if (new File(expandedPath).isAbsolute()) {
            return new File(expandedPath);
        }
        return new File(FileResources.resultDir(), expandedPath);
    }

    public static File resultDir() {
        return Optional.ofNullable(System.getProperty(PROPERTY_RESULT_BASE))
                .map(FileResources::expandEnvironmentVariables)
                .map(File::new)
                .orElse(FileResources.baseDir());
    }

    public static File datasetDir() {
        return Optional.ofNullable(System.getProperty(PROPERTY_DATASET_BASE))
                .map(FileResources::expandEnvironmentVariables)
                .map(File::new)
                .orElse(FileResources.baseDir());
    }

    public static File baseDir() {
        return Optional.ofNullable(System.getProperty(PROPERTY_WORKSPACE))
                .filter(it -> !it.isEmpty())
                .map(FileResources::expandEnvironmentVariables)
                .map(File::new)
                .orElse(new File("."));
    }

    public static File settingDir() {
        return new File(baseDir(), RESOURCES_SETTING_PATH);
    }

    public static File templateFileDir() {
        return new File(baseDir(), RESOURCES_TEMPLATE_PATH);
    }

    public static File jdbcPropDir() {
        return new File(baseDir(), RESOURCES_JDBC_PATH);
    }

    public static File xlsxSchemaDir() {
        return new File(baseDir(), RESOURCES_XLSX_SCHEMA_PATH);
    }

    public static File parameterizeTemplateDir() {
        return new File(baseDir(), OPTION_PARAMETERIZE_TEMPLATE_PATH);
    }

}
