package yo.dbunitcli.resource;


import yo.dbunitcli.Strings;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum FileResources {
    SINGLETON;

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

    public static File searchInOrderDatasetBase(final String path) {
        return FileResources.searchInOrder(path, FileResources.getDatasetBase(), FileResources::searchInOrderWorkspace);
    }

    public static File searchInOrderWorkspace(final String path) {
        return FileResources.searchInOrder(path, FileResources.getWorkspace(), File::new);
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
        final String resultBase = FileResources.getResultBase();
        return Strings.isNotEmpty(resultBase)
                ? new File(resultBase)
                : FileResources.baseDir();
    }

    public static File datasetDir() {
        final String resultBase = FileResources.getDatasetBase();
        return Strings.isNotEmpty(resultBase)
                ? new File(resultBase)
                : FileResources.baseDir();
    }

    public static File baseDir() {
        return Optional.of(FileResources.getWorkspace())
                .filter(it -> !it.isEmpty())
                .map(File::new)
                .orElse(new File("."));
    }

    private static String getWorkspace() {
        return System.getProperty(FileResources.PROPERTY_WORKSPACE, "");
    }

    private static String getDatasetBase() {
        return System.getProperty(FileResources.PROPERTY_DATASET_BASE, "");
    }

    private static String getResultBase() {
        return System.getProperty(FileResources.PROPERTY_RESULT_BASE, "");
    }

}
