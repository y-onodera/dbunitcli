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
    static FileResourcesContext context = new CLIFileResourcesContext();

    public static void setContext(final FileResourcesContext newContext) {
        context = newContext;
    }

    public static String readClasspathResource(final String aURL) {
        return FileResources.readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(final String aURL, final Charset aCharset) {
        try (final InputStream inputStream = FileResources.class.getClassLoader().getResourceAsStream(aURL)) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), aCharset))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
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

    public static File searchDatasetBase(final String path) {
        return FileResources.searchInOrder(path, context.datasetBase(), FileResources::searchWorkspace);
    }

    public static File searchSetting(final String settingPath) {
        return FileResources.searchInOrder(settingPath, context.settingBase(), FileResources::searchWorkspace);
    }

    public static File searchTemplate(final String templatePath) {
        if (Strings.isEmpty(templatePath)) {
            return null;
        }
        return FileResources.searchInOrder(templatePath, context.templateBase(), FileResources::searchWorkspace);
    }

    public static File searchJdbc(final String jdbcPath) {
        return FileResources.searchInOrder(jdbcPath, context.jdbcBase(), FileResources::searchWorkspace);
    }

    public static File searchXlsxSchema(final String xlsxSchemaPath) {
        if (Strings.isEmpty(xlsxSchemaPath)) {
            return null;
        }
        return FileResources.searchInOrder(xlsxSchemaPath, context.xlsxSchemaBase(), FileResources::searchWorkspace);
    }

    public static File searchWorkspace(final String path) {
        return FileResources.searchInOrder(path, context.workspace(), File::new);
    }

    private static File searchInOrder(final String path, final String parent, final Function<String, File> next) {
        if (new File(path).isAbsolute()) {
            return new File(path);
        }
        if (Strings.isNotEmpty(parent)) {
            final File candidate = new File(parent, path);
            if (candidate.exists()) {
                return candidate;
            }
        }
        return next.apply(path);
    }

    public static File resultDir(final String path) {
        if (Strings.isEmpty(path)) {
            return FileResources.resultDir();
        }
        if (new File(path).isAbsolute()) {
            return new File(path);
        }
        return new File(FileResources.resultDir(), path);
    }

    public static File resultDir() {
        final String resultBase = context.resultBase();
        return Strings.isNotEmpty(resultBase)
                ? new File(resultBase)
                : FileResources.baseDir();
    }

    public static File datasetDir() {
        final String resultBase = context.datasetBase();
        return Strings.isNotEmpty(resultBase)
                ? new File(resultBase)
                : FileResources.baseDir();
    }

    public static File baseDir() {
        return Optional.of(context.workspace())
                .filter(it -> !it.isEmpty())
                .map(File::new)
                .orElse(new File("."));
    }

    public interface FileResourcesContext {
        String datasetBase();

        String workspace();

        String resultBase();

        String settingBase();

        String templateBase();

        String jdbcBase();

        String xlsxSchemaBase();
    }

    record CLIFileResourcesContext() implements FileResourcesContext {

        @Override
        public String datasetBase() {
            return Optional.ofNullable(System.getProperty(PROPERTY_DATASET_BASE)).orElse("");
        }

        @Override
        public String workspace() {
            return Optional.ofNullable(System.getProperty(PROPERTY_WORKSPACE)).orElse("");
        }

        @Override
        public String resultBase() {
            return Optional.ofNullable(System.getProperty(PROPERTY_RESULT_BASE)).orElse("");
        }

        @Override
        public String settingBase() {
            return new File(this.workspace(), "resources/setting").getPath();
        }

        @Override
        public String templateBase() {
            return new File(this.workspace(), "resources/template").getPath();
        }

        @Override
        public String jdbcBase() {
            return new File(this.workspace(), "resources/jdbc").getPath();
        }

        @Override
        public String xlsxSchemaBase() {
            return new File(this.workspace(), "resources/xlsxschema").getPath();
        }
    }
}
