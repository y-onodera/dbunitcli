package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.sidecar.dto.ResourcesDto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public record Resources(
        File baseDir
        , List<String> jdbc
        , ResourceFile metadataSetting
        , List<String> template
        , ResourceFile xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    public ResourcesDto toDto() {
        final ResourcesDto result = new ResourcesDto();
        result.setDatasetSettings(this.metadataSetting().list());
        result.setXlsxSchemas(this.xlsxSchema().list());
        return result;
    }

    public static class Builder {
        private List<String> jdbc = new ArrayList<>();
        private List<String> template = new ArrayList<>();
        private File baseDir;

        public Resources build() {
            return new Resources(this.baseDir
                    , new ArrayList<>(this.jdbc)
                    , new ResourceFile(new File(this.baseDir, "dataset-setting"))
                    , new ArrayList<>(this.template)
                    , new ResourceFile(new File(this.baseDir, "xlsx-schema"))
            );
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "resources");
        }

        public Builder setJdbc(final List<String> jdbc) {
            this.jdbc = jdbc;
            return this;
        }

        public Builder setTemplate(final List<String> template) {
            this.template = template;
            return this;
        }

    }
}
