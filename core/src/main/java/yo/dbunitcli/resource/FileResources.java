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
        return Optional.ofNullable(System.getProperty(PROPERTY_RESULT_BASE))
                .map(File::new)
                .orElse(FileResources.baseDir());
    }

    public static File datasetDir() {
        return Optional.ofNullable(System.getProperty(PROPERTY_DATASET_BASE))
                .map(File::new)
                .orElse(FileResources.baseDir());
    }

    public static File baseDir() {
        return Optional.ofNullable(System.getProperty(PROPERTY_WORKSPACE))
                .filter(it -> !it.isEmpty())
                .map(File::new)
                .orElse(new File("."));
    }

    public static File settingDir() {
        return new File(baseDir(), "resources/setting");
    }

    public static File templateFileDir() {
        return new File(baseDir(), "resources/template");
    }

    public static File jdbcPropDir() {
        return new File(baseDir(), "resources/jdbc");
    }

    public static File xlsxSchemaDir() {
        return new File(baseDir(), "resources/xlsxSchema");
    }

}
