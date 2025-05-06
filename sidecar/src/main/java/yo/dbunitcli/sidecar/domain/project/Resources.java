package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.sidecar.dto.ResourcesDto;

import java.io.File;

public record Resources(
        File baseDir
        , ResourceFile jdbc
        , ResourceFile metadataSetting
        , ResourceFile template
        , ResourceFile xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    public ResourcesDto toDto() {
        final ResourcesDto result = new ResourcesDto();
        result.setDatasetSettings(this.metadataSetting().list());
        result.setJdbcFiles(this.jdbc().list());
        result.setTemplateFiles(this.template().list());
        result.setXlsxSchemas(this.xlsxSchema().list());
        return result;
    }

    public static class Builder {
        private File baseDir;

        public Resources build() {
            return new Resources(
                    this.baseDir,
                    new ResourceFile(new File(this.baseDir, "jdbc")),
                    new ResourceFile(new File(this.baseDir, "setting")),
                    new ResourceFile(new File(this.baseDir, "template")),
                    new ResourceFile(new File(this.baseDir, "xlsx-schema"))
            );
        }

        public Builder workspace(final File workspace) {
            return this.setBaseDir(new File(workspace, "resources"));
        }

        public Builder setBaseDir(final File baseDir) {
            this.baseDir = baseDir;
            return this;
        }
    }
}
