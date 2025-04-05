package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public record Datasource() {

    private static void create(final File file, final String contents) throws IOException {
        if (!file.exists()) {
            final File parent = file.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new RuntimeException("Failed to create directory: " + parent.getAbsolutePath());
                }
            }
            Files.createFile(file.toPath());
        }
        Files.writeString(file.toPath(), contents, StandardCharsets.UTF_8);
    }

    public void save(final DataSourceType type, final String fileName, final String contents) throws IOException {
        final File file = new File(fileName);
        if (file.isAbsolute()) {
            create(file, contents);
        } else {
            create(new File(new File(FileResources.datasetDir(), type.toString()), fileName), contents);
        }
    }

}
