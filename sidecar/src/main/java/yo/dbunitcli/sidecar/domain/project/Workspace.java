package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record Workspace(Path path, Options options, Resources resources) {

    public static Builder builder() {
        return new Builder();
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.options().parameterFiles(type);
    }

    public static class Builder {

        private String path;

        private static List<Path> loadFiles(final File baseDir, final String command) {
            final File subDir = new File(baseDir, command);
            final List<Path> files = new ArrayList<>();
            if (subDir.exists() && subDir.isDirectory()) {
                try {
                    files.addAll(Files.walk(subDir.toPath(), 1)
                            .filter(it -> !it.toAbsolutePath().toString().equals(subDir.getAbsolutePath()))
                            .toList());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return files;
        }

        public String getPath() {
            return this.path;
        }

        public Builder setPath(final String path) {
            this.path = path;
            return this;
        }

        public Workspace build() {
            final File baseDir = new File(Strings.isEmpty(this.path) ? "." : this.path);
            final Resources.Builder resources = Resources.builder();
            final Options.Builder options = Options.builder();
            if (baseDir.exists() || baseDir.mkdirs()) {
                final File optionDir = new File(baseDir, "option");
                if (optionDir.exists()) {
                    options.addParameterFiles(CommandType.compare, Builder.loadFiles(optionDir, "compare"))
                            .addParameterFiles(CommandType.convert, Builder.loadFiles(optionDir, "convert"))
                            .addParameterFiles(CommandType.generate, Builder.loadFiles(optionDir, "generate"))
                            .addParameterFiles(CommandType.run, Builder.loadFiles(optionDir, "run"));
                }
            }
            return new Workspace(baseDir.toPath(), options.build(), resources.build());
        }
    }
}
