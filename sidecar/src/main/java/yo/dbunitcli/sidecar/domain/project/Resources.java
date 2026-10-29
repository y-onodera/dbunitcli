package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ResourcesDto;

import java.io.File;

public record Resources(
        File baseDir
        , ResourceFile jdbc
        , ResourceFile datasetSetting
        , ResourceFile template
        , ResourceFile xlsxSchema
        , ResourceFile fixedColumnDef
        , ResourceFile sql
        , ResourceFile table
        , ResourceFile csvq) {

    public static Builder builder() {
        return new Builder();
    }

    public ResourcesDto toDto() {
        final ResourcesDto result = new ResourcesDto();
        result.setDatasetSettings(this.datasetSetting().list());
        result.setJdbcFiles(this.jdbc().list());
        result.setTemplateFiles(this.template().list());
        result.setXlsxSchemas(this.xlsxSchema().list());
        result.setFixedColumnDefs(this.fixedColumnDef().list());
        result.setSqlFiles(this.sql().list());
        result.setTableFiles(this.table().list());
        result.setCsvqFiles(this.csvq().list());
        return result;
    }

    public static class Builder {
        private File baseDir;

        public Resources build() {
            return new Resources(
                    this.baseDir,
                    new ResourceFile(new File(this.baseDir, "jdbc"), FileResources::searchJdbc),
                    new ResourceFile(new File(this.baseDir, "setting"), FileResources::searchSetting),
                    new ResourceFile(new File(this.baseDir, "template"), FileResources::searchTemplate),
                    new ResourceFile(new File(this.baseDir, "xlsx-schema"), FileResources::searchXlsxSchema),
                    new ResourceFile(new File(this.baseDir, "fixed-column-def"), FileResources::searchFixedColumnDef),
                    new ResourceFile(new File(this.baseDir, "sql"), FileResources::searchSql),
                    new ResourceFile(new File(this.baseDir, "table"), FileResources::searchTable),
                    new ResourceFile(new File(this.baseDir, "csvq"), FileResources::searchCsvq)
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
