package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.util.List;

public record Datasource(DataSourceType type, ResourceFile resourceFile) {
    public Datasource(final DataSourceType type) {
        this(type, new ResourceFile(new File(FileResources.datasetDir(), type.toString())));
    }

    public void save(final String fileName, final String contents) throws IOException {
        this.resourceFile.update(fileName, contents);
    }

    public List<String> list() {
        return this.resourceFile.list();
    }

    public String read(final String fileName) {
        return this.resourceFile.read(fileName).orElse("");
    }

    public void delete(final String fileName) throws IOException {
        this.resourceFile.delete(fileName);
    }
}
