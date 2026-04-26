package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.resource.FileResources;

import java.io.IOException;
import java.util.List;

public record Datasource(ResourceFile resourceFile) {
    public Datasource() {
        this(new ResourceFile(FileResources.datasetDir(), FileResources::searchDatasetBase));
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
